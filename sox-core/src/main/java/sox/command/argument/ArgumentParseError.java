package sox.command.argument;

import sox.command.AbstractContext;

/**
 * Thrown when an argument cannot be parsed on methods that must return a valid parsed argument.
 */
public class ArgumentParseError extends RuntimeException {
    private final AbstractContext context;
    private final Parser parser;
    private final Arguments readArguments;

    public ArgumentParseError(AbstractContext context, Parser parser, Arguments readArguments) {
        super("Unable to parse argument using parser " + parser + " and arguments " + readArguments);
        this.context = context;
        this.parser = parser;
        this.readArguments = readArguments;
    }

    /**
     * Context for the current command call.
     *
     * @return Context for the command.
     */
    public AbstractContext context() {
        return context;
    }

    /**
     * Parser that failed to yield a valid value.
     *
     * @return The failing parser.
     */
    public Parser parser() {
        return parser;
    }

    /**
     * Arguments that were used by the parser.
     *
     * @return Arguments used by the parser.
     */
    public Arguments readArguments() {
        return readArguments;
    }
}
