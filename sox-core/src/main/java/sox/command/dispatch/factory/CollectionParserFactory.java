package sox.command.dispatch.factory;

import sox.command.argument.MarkedBlock;
import sox.command.argument.Parser;
import sox.command.dispatch.ParserRegistry;
import sox.command.dispatch.config.NotEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class CollectionParserFactory<T> implements ParserFactory<Collection<T>> {
    private final Supplier<? extends Collection<T>> collectionFactory;

    public CollectionParserFactory(Supplier<? extends Collection<T>> collectionFactory) {
        this.collectionFactory = collectionFactory;
    }

    @Override
    public Parser<Collection<T>> create(ParserRegistry registry, Type[] typeParameters, Annotation[] annotations) {
        if(typeParameters.length == 0) {
            throw new IllegalArgumentException("Raw types are not supported");
        }
        Parser<?> elementParser = registry.resolve(typeParameters[0], annotations);
        boolean notEmpty = hasNotEmpty(annotations);
        return (context, arguments) -> {
            Collection<T> collection = collectionFactory.get();
            MarkedBlock block = arguments.marked();
            for(Optional<?> element = context.tryArgument(elementParser); element.isPresent(); element = context.tryArgument(elementParser)) {
                collection.add(uncheckedCast(element.get()));
                block.mark();
            }
            block.reset();
            return notEmpty && collection.isEmpty() ? Optional.empty() : Optional.of(collection);
        };
    }

    private static boolean hasNotEmpty(Annotation[] array) {
        for(Annotation a : array) {
            if(a instanceof NotEmpty) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> T uncheckedCast(Object object) {
        return (T)object;
    }
}
