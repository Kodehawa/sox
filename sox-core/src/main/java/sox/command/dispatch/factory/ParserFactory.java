package sox.command.dispatch.factory;

import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface ParserFactory {
    Parser<?> create(ParserRegistry registry, Type[] typeParameters, Annotation[] argumentAnnotations);
}