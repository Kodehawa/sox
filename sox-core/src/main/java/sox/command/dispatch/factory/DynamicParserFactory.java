package sox.command.dispatch.factory;

import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface DynamicParserFactory {
    @Nullable
    Parser<?> create(ParserRegistry registry, Type type, Annotation[] annotations);
}
