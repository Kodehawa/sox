package sox.command.argument;

import sox.command.AbstractContext;
import sox.util.ThrowingFunction;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

/**
 * Similar to {@link BasicParser}, but catches any thrown exceptions and allows having custom handling
 * of them them, or defaulting to an empty result.
 *
 * @param <T> esulting type of the transformation.
 */
public class CatchingParser<T> implements Parser<T> {
    private final ThrowingFunction<String, T> parseFunction;
    private final Function<Exception, Optional<T>> errorHandler;

    public CatchingParser(@Nonnull ThrowingFunction<String, T> parseFunction, @Nonnull Function<Exception, Optional<T>> errorHandler) {
        this.parseFunction = parseFunction;
        this.errorHandler = errorHandler;
    }

    public CatchingParser(@Nonnull ThrowingFunction<String, T> parseFunction) {
        this(parseFunction, __->Optional.empty());
    }

    @Nonnull
    @Override
    public Optional<T> parse(@Nonnull AbstractContext context, @Nonnull Arguments arguments) {
        try {
            return Optional.of(parseFunction.apply(arguments.next().getValue()));
        } catch(Exception e) {
            return errorHandler.apply(e);
        }
    }
}
