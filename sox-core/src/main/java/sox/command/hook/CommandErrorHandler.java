package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Called when a command throws an exception.
 *
 * @param <C> Type of the context implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface CommandErrorHandler<C extends AbstractContext<C>> {
    /**
     * Called when a command throws an exception.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     * @param e Exception thrown.
     *
     * @return True, if the exception was handled. Returning true means <b>no other exception hooks will be called</b>.
     */
    @CheckReturnValue
    boolean onCommandError(@Nonnull C context, @Nonnull AbstractCommand<C> command, @Nonnull Exception e);

    /**
     * Creates an error handler that delegates to a listener. It will not mark the exception as handled.
     *
     * @param listener Exception handler.
     * @param <C> Type of the context implementation.
     *
     * @return An error handler that delegates to a listener.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandErrorHandler<C> fromListener(@Nonnull CommandErrorListener<C> listener) {
        return (ctx, cmd, e) -> {
            listener.onCommandError(ctx, cmd, e);
            return false;
        };
    }

    /**
     * Returns an error handler that delegates to a predicate. If the predicate returns true,
     * other error handlers will not be called.
     *
     * @param predicate Exception handler.
     * @param <C> Type of the context implementation.
     *
     * @return An error handler that delegates to a given consumer.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandErrorHandler<C> fromPredicate(@Nonnull Predicate<Exception> predicate) {
        return (__, ___, e) -> predicate.test(e);
    }

    /**
     * Returns an error handler that calls the given consumer. It will not mark the exception as handled.
     *
     * @param consumer Exception handler.
     * @param <C> Type of the context implementation.
     *
     * @return An error handler that delegates to a given consumer.
     */
    @Nonnull
    @CheckReturnValue
    static <C extends AbstractContext<C>> CommandErrorHandler<C> fromConsumer(@Nonnull Consumer<Exception> consumer) {
        return (__, ___, e) -> {
            consumer.accept(e);
            return false;
        };
    }

    /**
     * Returns an error handler that ignores the provided exception classes.
     *
     * @param classes Classes to ignore.
     * @param <C> Type of the context implementation.
     *
     * @return An error handler that ignores provided classes.
     *
     * @apiNote This method checks if the exception is an instance of a provided class,
     *          <b>not exact class matching</b>. Providing a class will ignore all of
     *          it's subtypes as well.
     */
    @Nonnull
    @CheckReturnValue
    @SafeVarargs
    static <C extends AbstractContext<C>> CommandErrorHandler<C> ignore(@Nonnull Class<? extends Exception>... classes) {
        return (__, ___, e) -> {
            for(Class<? extends Exception> c : classes) {
                if(c.isInstance(e)) return true;
            }
            return false;
        };
    }

    /**
     * Called when a command throws an exception.
     *
     * @param <C> Type of the context implementation.
     */
    @FunctionalInterface
    interface CommandErrorListener<C extends AbstractContext<C>> {
        /**
         * Called when a command throws an exception.
         *
         * @param context Context for the command call.
         * @param command Command being called.
         * @param e Exception thrown.
         */
        void onCommandError(@Nonnull AbstractContext<C> context, @Nonnull AbstractCommand<C> command, @Nonnull Exception e);
    }
}
