package sox.command.dispatch;

import sox.command.AbstractContext;
import sox.command.argument.MarkedBlock;
import sox.command.argument.Parser;
import sox.command.argument.Parsers;
import sox.command.dispatch.config.AllowedProtocols;
import sox.command.dispatch.config.Http;
import sox.command.dispatch.config.IgnoreCase;
import sox.command.dispatch.config.Joining;
import sox.command.dispatch.config.Matching;
import sox.command.dispatch.config.NotEmpty;
import sox.command.dispatch.config.RemainingContent;
import sox.command.dispatch.factory.CollectionParserFactory;
import sox.command.dispatch.factory.DynamicParserFactory;
import sox.command.dispatch.factory.ParserFactory;
import sox.command.dispatch.factory.PossiblyLenientParserFactory;
import sox.util.ListFactory;
import sox.util.MapFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ParserRegistry {
    private static final Map<TypeWrapper, ParserFactory> DEFAULT_FACTORIES = new HashMap<TypeWrapper, ParserFactory>(){{
        ParserFactory intFactory = new PossiblyLenientParserFactory(Parsers.strictInt(), Parsers.lenientInt());
        put(wrap(int.class), intFactory);
        put(wrap(Integer.class), intFactory);

        ParserFactory longFactory = new PossiblyLenientParserFactory(Parsers.strictLong(), Parsers.lenientLong());
        put(wrap(long.class), longFactory);
        put(wrap(Long.class), longFactory);

        put(wrap(float.class), wrap(Parsers.parseFloat()));
        put(wrap(Float.class), wrap(Parsers.parseFloat()));
        put(wrap(double.class), wrap(Parsers.parseDouble()));
        put(wrap(Double.class), wrap(Parsers.parseDouble()));

        put(wrap(List.class), new CollectionParserFactory(ArrayList::new));
        put(wrap(Set.class), new CollectionParserFactory(HashSet::new));
        put(wrap(Collection.class), new CollectionParserFactory(ArrayList::new));
        put(wrap(Iterable.class), new CollectionParserFactory(ArrayList::new));
        put(wrap(Iterator.class), (registry, types, annotations) -> {
            Parser<?> elementParser = registry.resolve(types[0], annotations);
            boolean notEmpty = has(annotations, NotEmpty.class);
            return (context, arguments) -> {
                Collection<?> collection = new ArrayList<>();
                MarkedBlock block = arguments.marked();
                for(Optional<?> element = context.tryArgument(elementParser); element.isPresent(); element = context.tryArgument(elementParser)) {
                    collection.add(uncheckedCast(element.get()));
                    block.mark();
                }
                block.reset();
                return notEmpty && collection.isEmpty() ? Optional.empty() : Optional.of(collection.iterator());
            };
        });

        put(wrap(URL.class), (__1, __2, annotations) -> {
            List<String> list = new ArrayList<>();
            if(has(annotations, Http.class)) {
                list.add("http");
                list.add("https");
            }
            AllowedProtocols ap = find(annotations, AllowedProtocols.class);
            if(ap != null) {
                Collections.addAll(list, ap.protocols());
            }
            return list.isEmpty() ? Parsers.url() : Parsers.url(list);
        });

        put(wrap(String.class), (__1, __2, annotations) -> {
            Matching m = find(annotations, Matching.class);
            if(m != null) {
                return Parsers.matching(m.pattern(), m.flags());
            }
            Joining j = find(annotations, Joining.class);
            if(j != null) {
                return Parsers.remainingArguments(j.separator());
            }
            if(has(annotations, RemainingContent.class)) {
                return Parsers.remainingContent();
            }
            return Parsers.string();
        });
    }};
    @SuppressWarnings("unchecked")
    private static final List<DynamicParserFactory> DEFAULT_DYNAMIC_FACTORIES = new ArrayList<DynamicParserFactory>() {{
        add((__, type, annotations) -> {
            if(type instanceof Class && ((Class)type).isEnum()) {
                return Parsers.toEnum((Class<? extends Enum>)type, has(annotations, IgnoreCase.class));
            }
            return null;
        });
        add((__, type, annotations) -> {
            if(type instanceof Class && AbstractContext.class.isAssignableFrom(((Class)type))) {
                return (c, ___) -> Optional.of(c);
            }
            if(type instanceof ParameterizedType &&
                    AbstractContext.class.isAssignableFrom((Class)((ParameterizedType)type).getRawType())) {
                return (c, ___) -> Optional.of(c);
            }
            return null;
        });
    }};

    private final Map<TypeWrapper, ParserFactory> factories;
    private final List<DynamicParserFactory> dynamicFactories;

    public ParserRegistry(MapFactory mapFactory, ListFactory listFactory) {
        this.factories = mapFactory.create();
        this.factories.putAll(DEFAULT_FACTORIES);
        this.dynamicFactories = listFactory.create();
        this.dynamicFactories.addAll(DEFAULT_DYNAMIC_FACTORIES);
    }

    public ParserRegistry() {
        this(HashMap::new, ArrayList::new);
    }

    public <T> void register(Class<T> tClass, Parser<T> parser) {
        factories.put(wrap(tClass), wrap(parser));
    }

    public void register(Type type, ParserFactory factory) {
        factories.put(wrap(type), factory);
    }

    public void registerDynamic(DynamicParserFactory factory) {
        dynamicFactories.add(factory);
    }

    public Parser<?>[] resolve(Method method) {
        Type[] argTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        Parser<?>[] parsers = new Parser[argTypes.length];
        for(int i = 0; i < parsers.length; i++) {
            parsers[i] = resolve(argTypes[i], annotations[i]);
        }
        return parsers;
    }

    public Parser<?> resolve(Type type, Annotation[] annotations) {
        Parser<?> resolved = resolve0(type, annotations);
        if(resolved == null) {
            throw new IllegalArgumentException("Unable to resolve parser for type " + type);
        }
        return resolved;
    }

    private Parser<?> resolve0(Type type, Annotation[] annotations) {
        ParserFactory f = factories.get(wrap(type));
        if(f != null) {
            return f.create(this, type instanceof ParameterizedType ?
                    ((ParameterizedType)type).getActualTypeArguments() : new Type[0],
                    annotations);
        }
        for(DynamicParserFactory df : dynamicFactories) {
            Parser<?> p = df.create(this, type, annotations);
            if(p != null) return p;
        }
        return null; //resolve will throw
    }

    private static ParserFactory wrap(Parser<?> parser) {
        return (__1, __2, __3) -> parser;
    }

    private static TypeWrapper wrap(Type type) {
        if(type instanceof ParameterizedType) {
            for(Type t : ((ParameterizedType)type).getActualTypeArguments()) {
                wrap(t); //ensure valid
            }
        } else if(!(type instanceof Class)) {
            throw new IllegalArgumentException("Variable types cannot be used with the dynamic dispatcher! Bad type: " + type);
        }
        return new TypeWrapper(type);
    }

    private static boolean has(Annotation[] array, Class<? extends Annotation> annotationClass) {
        return find(array, annotationClass) != null;
    }

    private static <T extends Annotation> T find(Annotation[] array, Class<T> annotationClass) {
        for(Annotation a : array) {
            if(annotationClass.isInstance(a)) return annotationClass.cast(a);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object object) {
        return (T)object;
    }

    //used for overriding equals check
    private static class TypeWrapper {
        private final Type type;

        TypeWrapper(Type type) {
            this.type = type;
        }

        @Override
        public int hashCode() {
            if(type instanceof Class) {
                return type.hashCode();
            }
            if(type instanceof ParameterizedType) {
                return ((ParameterizedType) type).getRawType().hashCode();
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TypeWrapper && checkType(((TypeWrapper)obj).type);
        }

        private boolean checkType(Type other) {
            return getTypeForComparison(type).equals(getTypeForComparison(other));
        }

        private static Type getTypeForComparison(Type t) {
            if(t instanceof Class) {
                return t;
            }
            if(t instanceof ParameterizedType) {
                return ((ParameterizedType) t).getRawType();
            }
            return t;
        }
    }
}
