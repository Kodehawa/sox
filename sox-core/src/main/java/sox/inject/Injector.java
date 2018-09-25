package sox.inject;

import sox.service.ServiceManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Instantiates classes by finding a suitable constructor. Arguments are searched
 * for in a {@link ServiceManager service manager}.
 */
public class Injector {
    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new ConstructorComparator();

    private final ServiceManager serviceManager;

    public Injector(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * Attempts to instantiate the given class.
     *
     * @param targetClass Class to attempt instantiating.
     * @param <T> Type of the class.
     *
     * @return An instance of the given class.
     *
     * @throws IllegalArgumentException If the class is not instantiable or no suitable constructors are found.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    public <T> T instantiate(@Nonnull Class<T> targetClass) {
        if(targetClass.isPrimitive()) {
            throw new IllegalArgumentException("Cannot instantiate primitive classes");
        }
        if(targetClass.isEnum()) {
            throw new IllegalArgumentException("Cannot instantiate enums");
        }
        if(targetClass.isArray()) {
            throw new IllegalArgumentException("Cannot instantiate arrays");
        }
        if(targetClass.isInterface()) {
            throw new IllegalArgumentException("Cannot instantiate interfaces");
        }
        if(Modifier.isAbstract(targetClass.getModifiers())) {
            throw new IllegalArgumentException("Cannot instantiate abstract classes");
        }
        if(targetClass.isMemberClass() && !Modifier.isStatic(targetClass.getModifiers())) {
            throw new IllegalArgumentException("Cannot instantiate non static inner classes");
        }
        List<Constructor<?>> list = new ArrayList<>();
        for(Constructor<?> constructor : targetClass.getConstructors()) {
            if(isValid(constructor)) {
                list.add(constructor);
            }
        }
        list.sort(CONSTRUCTOR_COMPARATOR);
        for(Constructor<?> constructor : list) {
            Object instance = tryConstructor(constructor);
            if(instance != null) {
                return (T)instance;
            }
        }
        throw new IllegalArgumentException("Unable to find suitable constructor");
    }

    private Object tryConstructor(Constructor<?> constructor) {
        Class<?>[] argClasses = constructor.getParameterTypes();
        Annotation[][] argAnnotations = constructor.getParameterAnnotations();
        Object[] args = new Object[argClasses.length];
        for(int i = 0; i < argClasses.length; i++) {
            Annotation[] annotations = argAnnotations[i];
            Set<?> found = serviceManager.findServices(argClasses[i], false);
            if(found.isEmpty()) {
                found = serviceManager.findServices(argClasses[i], true);
                if(found.isEmpty()) {
                    boolean allowNull = false;
                    for(Annotation annotation : annotations) {
                        if(annotation instanceof AllowNull) {
                            allowNull = true;
                            break;
                        }
                    }
                    if(!allowNull) return null;
                }
            }
            if(found.isEmpty()) continue;
            args[i] = found.size() == 1 ? found.iterator().next() : findBestMatch(argClasses[i], found);
        }
        try {
            return constructor.newInstance(args);
        } catch(IllegalAccessException e) {
            throw new AssertionError(e);
        } catch(InstantiationException|InvocationTargetException e) {
            return null;
        }
    }

    private static boolean isValid(Constructor<?> constructor) {
        return constructor.getAnnotation(InjectIgnore.class) == null;
    }

    private static Object findBestMatch(Class<?> target, Set<?> values) {
        return values.stream()
                .map(v->new WeightedValue(v, distance(target, v)))
                .min(Comparator.comparingInt(v->v.weight))
                .map(v->v.value)
                .orElseThrow(AssertionError::new);
    }

    private static int distance(Class<?> target, Object value) {
        Class<?> c = value.getClass();
        int distance = 0;
        while(c != null) {
            if(c == target) {
                return distance;
            }
            distance++;
            int i = interfaceDistance(c, target, distance);
            if(i != -1) return i;
            c = c.getSuperclass();
        }
        throw new AssertionError();
    }

    private static int interfaceDistance(Class<?> source, Class<?> target, int distance) {
        for(Class<?> c : source.getInterfaces()) {
            if(c == target) return distance;
            int i = interfaceDistance(c, target, distance + 1);
            if(i != -1) return i;
        }
        return -1;
    }

    private static class WeightedValue {
        final Object value;
        final int weight;

        private WeightedValue(Object value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    private static class ConstructorComparator implements Comparator<Constructor<?>> {
        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            int i = compareWeights(o1, o2);
            if(i != 0) return i;
            Class<?>[] args1 = o1.getParameterTypes();
            Class<?>[] args2 = o2.getParameterTypes();
            if(args1.length > args2.length) return -1;
            if(args2.length > args1.length) return 1;
            return compareSpecificity(args1, args2);
        }

        private static int compareWeights(Constructor<?> o1, Constructor<?> o2) {
            InjectWeight w1 = o1.getAnnotation(InjectWeight.class);
            InjectWeight w2 = o2.getAnnotation(InjectWeight.class);
            if(w1 == null && w2 == null) return 0;
            if(w1 == null) {
                return 1;
            }
            if(w2 == null) {
                return -1;
            }
            return w2.value() - w1.value();
        }

        private static int compareSpecificity(Class<?>[] args1, Class<?>[] args2) {
            for(int i = 0; i < args1.length; i++) {
                Class<?> c1 = args1[i];
                Class<?> c2 = args2[i];
                if(c1.isAssignableFrom(c2)) {
                    return 1;
                }
                if(c2.isAssignableFrom(c1)) {
                    return -1;
                }
            }
            return 0;
        }
    }
}
