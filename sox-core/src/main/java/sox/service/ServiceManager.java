package sox.service;

import sox.util.MapFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers objects based on their types (including supertypes and interfaces), providing lookups
 * based on any of those.
 */
public class ServiceManager {
    private final MapFactory factory;
    private final Map<Class<?>, Object> directMap;
    private final Map<Class<?>, List<IndirectEntry>> indirectMap;
    private final Map<Class<?>, Set<Class<?>>> subclassMap;

    public ServiceManager(@Nonnull MapFactory factory) {
        this.factory = factory;
        this.directMap = factory.create();
        this.indirectMap = factory.create();
        this.subclassMap = factory.create();
    }

    public ServiceManager() {
        this(HashMap::new);
    }

    /**
     * Registers a new service.
     *
     * @param service Service to register.
     *
     * @see #registerService(Object, boolean)
     */
    public void registerService(@Nonnull Object service) {
        registerService(service, false);
    }

    /**
     * Registers a new service.
     *
     * @param service Service to register.
     * @param override Whether or not a registered service of the same type should be replaced, if it exists.
     *                 If false, and a service for the given type already exists, an exception is thrown.
     */
    public synchronized void registerService(@Nonnull Object service, boolean override) {
        Objects.requireNonNull(service, "Service may not be null");
        Class<?> serviceClass = service.getClass();
        Object existing = directMap.get(serviceClass);
        if(existing != null && !override) {
            throw new DuplicateServiceDefinitionException(serviceClass);
        }
        directMap.put(serviceClass, service);
        register(new IndirectEntry(serviceClass, service));
    }

    /**
     * Returns a service matching the given class. Throws if the number of matching services is not 1.
     *
     * @param serviceClass Class of the wanted service.
     * @param <T> Type of the wanted service.
     *
     * @return An instance of the wanted service
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    public synchronized <T> T getService(@Nonnull Class<T> serviceClass) {
        Set<T> set = findServices(serviceClass, false);
        switch(set.size()) {
            case 0: throw new NoServiceMatchException(serviceClass);
            case 1: return set.iterator().next();
            default: throw new MultipleServiceMatchException(serviceClass);
        }
    }

    /**
     * Returns a set of services matching the given class.
     *
     * @param serviceClass Class to look for.
     * @param fullSearch If false, the search will stop and return as soon as a matching service is found.
     *                   <b>This does not mean the list will have only one element</b>
     * @param <T> Type of the wanted service.
     *
     * @return Set of services matching the given class.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    public synchronized <T> Set<T> findServices(@Nonnull Class<T> serviceClass, boolean fullSearch) {
        Objects.requireNonNull(serviceClass, "Service class may not be null");
        Set<T> set = new HashSet<>();
        Object instance = directMap.get(serviceClass);
        if(instance != null) {
            set.add((T)instance);
            if(!fullSearch) return set;
        }
        set.addAll(convert(fromList(indirectMap.get(serviceClass))));
        if(!fullSearch && !set.isEmpty()) return set;
        set.addAll(convert(findByInterfaces(serviceClass, fullSearch)));
        if(!fullSearch && !set.isEmpty()) return set;
        set.addAll(convert(findBySubclasses(serviceClass, fullSearch)));
        return set;
    }

    /**
     * Returns a snapshot of this manager. Changes made to this manager or the snapshot will
     * have no effect on the other.
     *
     * @param factory Factory used to instantiate the internal maps.
     *
     * @return A snapshot of this manager.
     */
    @Nonnull
    @CheckReturnValue
    public ServiceManager snapshot(@Nonnull MapFactory factory) {
        ServiceManager sm = new ServiceManager(factory);
        sm.directMap.putAll(directMap);
        indirectMap.forEach((k, v) -> {
            List<IndirectEntry> copy = new ArrayList<>(v);
            sm.indirectMap.put(k, copy);
        });
        subclassMap.forEach((k, v) -> {
            Set<Class<?>> copy = new HashSet<>(v);
            sm.subclassMap.put(k, copy);
        });
        return sm;
    }

    /**
     * Returns a snapshot of this manager. Changes made to this manager or the snapshot will
     * have no effect on the other.
     *
     * @return A snapshot of this manager.
     */
    @Nonnull
    @CheckReturnValue
    public ServiceManager snapshot() {
        return snapshot(factory);
    }

    private void register(@Nonnull IndirectEntry entry) {
        Class<?> c = entry.realClass;
        while(c != null) {
            List<IndirectEntry> list = indirectMap.computeIfAbsent(c, k->new ArrayList<>());
            list.removeIf(e->e.realClass == entry.realClass);
            list.add(entry);
            registerByInterfaces(c, entry);
            Class<?> superclass = c.getSuperclass();
            if(superclass != null) {
                subclassMap.computeIfAbsent(superclass, k->new HashSet<>()).add(c);
            }
            c = superclass;
        }
    }

    private void registerByInterfaces(@Nonnull Class<?> c, @Nonnull IndirectEntry entry) {
        Class<?>[] interfaces = c.getInterfaces();
        for(Class<?> itf : interfaces) {
            registerByInterfaces(itf, entry);
            List<IndirectEntry> list = indirectMap.computeIfAbsent(itf, k->new ArrayList<>());
            list.removeIf(e->e.realClass == entry.realClass);
            list.add(entry);
        }
    }

    @Nonnull
    @CheckReturnValue
    private List<Object> findBySubclasses(@Nonnull Class<?> search, boolean fullSearch) {
        List<Object> res = new ArrayList<>();
        Set<Class<?>> subclasses = subclassMap.get(search);
        if(subclasses == null) return res;
        for(Class<?> sub : subclasses) {
            List<Object> list = fromList(indirectMap.get(sub));
            if(!list.isEmpty()) {
                res.addAll(list);
                if(!fullSearch && !res.isEmpty()) return res;
            }
            res.addAll(findByInterfaces(sub, fullSearch));
            if(!fullSearch && !res.isEmpty()) return res;
            res.addAll(findBySubclasses(sub, fullSearch));
            if(!fullSearch && !res.isEmpty()) return res;
        }
        return res;
    }

    @Nonnull
    @CheckReturnValue
    private List<Object> findByInterfaces(@Nonnull Class<?> search, boolean fullSearch) {
        List<Object> res = new ArrayList<>();
        Class<?>[] interfaces = search.getInterfaces();
        for(Class<?> c : interfaces) {
            List<Object> list = fromList(indirectMap.get(c));
            if(!list.isEmpty()) {
                res.addAll(list);
                if(!fullSearch && !res.isEmpty()) return res;
            }
            res.addAll(findByInterfaces(c, fullSearch));
            if(!fullSearch && !res.isEmpty()) return res;
        }
        return res;
    }

    @Nonnull
    @CheckReturnValue
    private static List<Object> fromList(@Nullable List<IndirectEntry> instances) {
        return instances == null ? Collections.emptyList() : instances.stream().map(x->x.instance).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    private static <T> List<T> convert(@Nonnull List<?> list) {
        return (List<T>)list;
    }

    private static class IndirectEntry {
        final Class<?> realClass;
        final Object instance;

        private IndirectEntry(@Nonnull Class<?> realClass, @Nonnull Object instance) {
            this.realClass = realClass;
            this.instance = instance;
        }

        @Override
        public int hashCode() {
            return realClass.hashCode() ^ instance.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IndirectEntry && ((IndirectEntry)obj).realClass == realClass;
        }

        @Override
        public String toString() {
            return instance.toString();
        }
    }
}
