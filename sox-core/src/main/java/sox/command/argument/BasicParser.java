package sox.command.argument;

import sox.command.AbstractContext;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

/**
 * Basic argument parsing, applying a transformation to a string.
 *
 * @param <T> Resulting type of the transformation.
 */
public class BasicParser<T> implements Parser<T> {
    private final Function<String, T> parseFunction;

    public BasicParser(@Nonnull Function<String, T> parseFunction) {
        this.parseFunction = parseFunction;
    }

    @Nonnull
    @Override
    public Optional<T> parse(@Nonnull AbstractContext context, @Nonnull Arguments arguments) {
        return Optional.of(parseFunction.apply(arguments.next().getValue()));
    }
}
