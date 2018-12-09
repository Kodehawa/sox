package sox.command.dispatch.factory;

import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

public interface ParserFactory<T> {
    Parser<T> create(ParserRegistry registry, Type[] typeParameters, Annotation[] argumentAnnotations);

    default <U> ParserFactory<U> map(Function<T, U> mapper) {
        return (r, t, a) -> create(r, t, a).map(mapper);
    }
}