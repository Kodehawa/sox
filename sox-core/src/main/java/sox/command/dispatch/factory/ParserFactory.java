package sox.command.dispatch.factory;

import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface ParserFactory<T> {
    Parser<T> create(ParserRegistry registry, Type[] typeParameters, Annotation[] argumentAnnotations);

    default <U> ParserFactory<U> map(Function<T, U> mapper) {
        return (r, t, a) -> create(r, t, a).map(mapper);
    }

    default <U> ParserFactory<U> flatMap(Function<T, Optional<U>> mapper) {
        return (r, t, a) -> create(r, t, a).flatMap(mapper);
    }

    default ParserFactory<T> filter(Predicate<T> filter) {
        return (r, t, a) -> create(r, t, a).filter(filter);
    }

    default <A extends Annotation> ParserFactory<T> replaceIfPresent(Class<A> annotationClass, Parser<T> newParser) {
        return replaceIfPresent(annotationClass, () -> newParser);
    }

    default <A extends Annotation> ParserFactory<T> replaceIfPresent(Class<A> annotationClass, Supplier<Parser<T>> newParser) {
        return (r, t, a) -> {
            for(Annotation annotation : a) {
                if(annotationClass.isInstance(annotation)) {
                    return newParser.get();
                }
            }
            return create(r, t, a);
        };
    }

    default <A extends Annotation> ParserFactory<T> replaceIfPresent(Class<A> annotationClass, Function<A, Parser<T>> newParser) {
        return (r, t, a) -> {
            for(Annotation annotation : a) {
                if(annotationClass.isInstance(annotation)) {
                    return newParser.apply(annotationClass.cast(annotation));
                }
            }
            return create(r, t, a);
        };
    }

    default <A extends Annotation> ParserFactory<T> mapIfPresent(Class<A> annotationClass, BiFunction<Parser<T>, A, Parser<T>> mapper) {
        return (r, t, a) -> {
            for(Annotation annotation : a) {
                if(annotationClass.isInstance(annotation)) {
                    return mapper.apply(create(r, t, a), annotationClass.cast(annotation));
                }
            }
            return create(r, t, a);
        };
    }

    default <A extends Annotation> ParserFactory<T> mapIfPresent(Class<A> annotationClass, UnaryOperator<Parser<T>> mapper) {
        return (r, t, a) -> {
            for(Annotation annotation : a) {
                if(annotationClass.isInstance(annotation)) {
                    return mapper.apply(create(r, t, a));
                }
            }
            return create(r, t, a);
        };
    }

    static <T> ParserFactory<T> of(Parser<T> parser) {
        return (__1, __2, __3) -> parser;
    }
}