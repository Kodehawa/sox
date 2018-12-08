package sox.command.dispatch.factory;

import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;
import sox.command.dispatch.config.Lenient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class PossiblyLenientParserFactory implements ParserFactory {
    private final Parser<?> strict;
    private final Parser<?> lenient;

    public PossiblyLenientParserFactory(Parser<?> strict, Parser<?> lenient) {
        this.strict = strict;
        this.lenient = lenient;
    }

    @Override
    public Parser<?> create(ParserRegistry registry, Type[] typeParameters, Annotation[] argumentAnnotations) {
        for(Annotation annotation : argumentAnnotations) {
            if(annotation instanceof Lenient) {
                return lenient;
            }
        }
        return strict;
    }
}
