package sox.command;

import sox.Sox;
import sox.command.argument.ArgumentParseError;
import sox.command.argument.Arguments;
import sox.command.argument.MarkedBlock;
import sox.command.argument.Parser;
import sox.command.argument.Parsers;
import sox.inject.Injector;
import sox.service.MultipleServiceMatchException;
import sox.service.ServiceManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class containing the context of a command call.
 * <br>Provides helpers for parsing arguments in an user friendly way
 * and handling command-local state, which only lives as long as the
 * command call does.
 *
 * @param <C> Type of the context implementation.
 */
public abstract class AbstractContext<C extends AbstractContext<C>> {
    protected final Sox sox;
    protected final Arguments arguments;
    protected ServiceManager serviceManager;

    protected AbstractContext(@Nonnull Sox sox, @Nonnull Arguments arguments) {
        this.sox = sox;
        this.arguments = arguments;
    }

    /**
     * Returns the {@link Sox sox} instance associated with this context.
     *
     * @return The sox instance.
     */
    @Nonnull
    @CheckReturnValue
    public Sox sox() {
        return sox;
    }

    /**
     * Returns the arguments provided for this context.
     *
     * @return The arguments provided.
     */
    @Nonnull
    @CheckReturnValue
    public Arguments arguments() {
        return arguments;
    }

    /**
     * Returns the {@link ServiceManager service manager} of this context.
     * <br>If no manager is available (first call to this method), the
     * service manager of the {@link Sox sox} instance of this context will
     * be copied and stored.
     * <br>Services registered to this manager <b>will not</b> affect the global
     * manager.
     *
     * @return The service manager.
     */
    @Nonnull
    @CheckReturnValue
    public synchronized ServiceManager serviceManager() {
        if(serviceManager == null) {
            serviceManager = sox.serviceManager().snapshot();
        }
        return serviceManager;
    }

    /**
     * Creates a best-effort copy of this context.
     * <br>Being a best-effort, the copy is not guaranteed to be a fully isolated
     * object, and certain changes made to it might affect the original.
     * <br>State stored by fields of subclasses, or in services registered
     * to the {@link #serviceManager() service manager} is not required to be
     * copied.
     *
     * @return A best-effort copy of this context.
     *
     * @implNote Implementations are required to create snapshots of both
     *           {@link #arguments() arguments} and the {@link #serviceManager() service manager}.
     */
    @Nonnull
    @CheckReturnValue
    public abstract C snapshot();

    /**
     * Finds a service by the given class.
     * <br>If a service manager specific to this context is available, it's
     * used for finding the service, otherwise the global one will be used.
     *
     * @param serviceClass Class of the wanted service.
     * @param <T> Type of the wanted service.
     *
     * @return An instance of the provided class.
     *
     * @see ServiceManager#getService(Class)
     */
    @Nonnull
    @CheckReturnValue
    public <T> T findService(@Nonnull Class<T> serviceClass) {
        ServiceManager sm = serviceManager == null ? sox.serviceManager() : serviceManager;
        return sm.getService(serviceClass);
    }

    /**
     * Attempts to find a service, or create one if none are found.
     * <br>If multiple are found, an exception is thrown.
     * <br>When creating a service, if a service manager specific to this
     * context is available, it's used for resolving constructor arguments.
     * The newly created service will also be registered to it.
     *
     * @param serviceClass Class of the wanted service.
     * @param <T> Type of the wanted service.
     *
     * @return An instance of the provided class.
     *
     * @see #findService(Class)
     * @see Injector#instantiate(Class)
     */
    @Nonnull
    @CheckReturnValue
    public <T> T service(@Nonnull Class<T> serviceClass) {
        ServiceManager sm = serviceManager == null ? sox.serviceManager() : serviceManager;
        Set<T> set = sm.findServices(serviceClass, false);
        switch(set.size()) {
            case 0:
                T instance = new Injector(sm).instantiate(serviceClass);
                if(serviceManager != null) serviceManager.registerService(instance);
                return instance;
            case 1: return set.iterator().next();
            default: throw new MultipleServiceMatchException(serviceClass);
        }
    }

    /**
     * Attempts to parse an argument with the provided {@link Parser parser}.
     * <br>If the parser returns {@link Optional#empty() nothing} or there are
     * no more arguments to read, an exception is thrown.
     *
     * @param parser Parser to use.
     * @param <T> Type of the object returned by the parser.
     *
     * @return The parsed object.
     *
     * @throws ArgumentParseError If there are no more arguments to read or the parser
     *                            returned nothing.
     */
    @Nonnull
    @CheckReturnValue
    public <T> T argument(@Nonnull Parser<T> parser) {
        return argument(parser, null);
    }

    /**
     * Attempts to parse an argument with the provided {@link Parser parser}.
     * <br>If the parser returns {@link Optional#empty() nothing} or there are
     * no more arguments to read, an exception is thrown.
     *
     * @param parser Parser to use.
     * @param failureMessage Message to provide to the {@link ArgumentParseError error}
     *                       thrown on parse failure.
     * @param <T> Type of the object returned by the parser.
     *
     * @return The parsed object.
     *
     * @throws ArgumentParseError If there are no more arguments to read or the parser
     *                            returned nothing.
     */
    @Nonnull
    @CheckReturnValue
    public <T> T argument(@Nonnull Parser<T> parser, @Nullable String failureMessage) {
        int offset = arguments.getOffset();
        Optional<T> optional;
        if(!arguments.hasNext()) {
            optional = Optional.empty();
        } else {
            optional = parser.parse(this);
        }
        return optional.orElseThrow(()->{
            Arguments copy = arguments.snapshot();
            copy.setOffset(offset);
            return new ArgumentParseError(failureMessage, this, parser, copy);
        });
    }

    /**
     * Attempts to parse an argument, returning to the previous state if parsing fails.
     * <br>Returns {@link Optional#empty() empty} if parsing fails or there are no more
     * arguments to read.
     * <br>If parsing fails, all arguments read by the parser are unread.
     *
     * @param parser Parser to use.
     * @param <T> Type of the object returned by the parser.
     *
     * @return An optional parsed argument.
     */
    @Nonnull
    @CheckReturnValue
    public <T> Optional<T> tryArgument(@Nonnull Parser<T> parser) {
        if(!arguments.hasNext()) return Optional.empty();
        MarkedBlock block = arguments.marked();
        Optional<T> optional = parser.parse(this);
        if(!optional.isPresent()) {
            block.reset();
        }
        return optional;
    }

    /**
     * Parses as many arguments as possible with the provided parser, stopping when parsing fails.
     * <br>Example:
     * Given the arguments <code>[1, 2, "abc"]</code>:
     * <pre><code>
     * List&lt;Integer&gt; ints = context.many({@link Parsers#strictInt()} Parsers.strictInt()});
     * assertEquals(ints.size(), 2);
     * assertEquals(ints.get(0), 1);
     * assertEquals(ints.get(1), 2);
     * String string = context.argument({@link Parsers#string() Parsers.string()});
     * assertEquals(string, "abc");
     * </code></pre>
     *
     * @param parser Parser to use.
     * @param <T> Type of the objects returned by the parser.
     *
     * @return A possibly empty list of arguments returned by the parser.
     */
    @Nonnull
    @CheckReturnValue
    public <T> List<T> many(@Nonnull Parser<T> parser) {
        List<T> list = new ArrayList<>();
        for(Optional<T> parsed = tryArgument(parser); parsed.isPresent(); parsed = tryArgument(parser)) {
            list.add(parsed.get());
        }
        return list;
    }

    /**
     * Returns whether or not the current argument can be parsed by the provided parser.
     * <br>Consumes the argument if it was parsed successfully.
     * <br>If parsing fails, all arguments read by the parser are unread.
     *
     * @param parser Parser to use.
     *
     * @return True if the current argument matched the parser.
     */
    @CheckReturnValue
    public boolean matches(@Nonnull Parser<?> parser) {
        return tryArgument(parser).isPresent();
    }

    /**
     * Reads arguments matching the provided parser, until either parsing fails or a delimiter is matched.
     * <br>Example:
     * Given the arguments <code>[1, 2, -1]</code>:
     * <pre><code>
     * List&lt;Integer&gt; ints = context.takeUntil({@link Parsers#strictInt() Parsers.strictInt()}, {@link Parsers#strictInt() Parsers.strictInt()}.{@link Parser#filter(Predicate) filter(x-&gt;x &lt; 0)});
     * assertEquals(ints.size(), 2);
     * assertEquals(ints.get(0), 1);
     * assertEquals(ints.get(1), 2);
     * Integer last = context.argument({@link Parsers#strictInt() Parsers.strictInt()});
     * assertEquals(last, -1);
     * </code></pre>
     *
     * @param valueParser Parser used for values.
     * @param delimiter Parser used for delimiter checking.
     * @param <T> Type of the objects returned by the value parser.
     *
     * @return Possibly empty list of arguments matching.
     */
    @Nonnull
    @CheckReturnValue
    public <T> List<T> takeUntil(Parser<T> valueParser, Parser<?> delimiter) {
        List<T> list = new ArrayList<>();
        MarkedBlock block = arguments.marked();
        if(tryArgument(delimiter).isPresent()) {
            block.reset();
            return list;
        }
        for(Optional<T> parsed = tryArgument(valueParser); parsed.isPresent(); parsed = tryArgument(valueParser)) {
            list.add(parsed.get());
            block.mark();
            if(tryArgument(delimiter).isPresent()) {
                block.reset();
                return list;
            }
        }
        return list;
    }
}
