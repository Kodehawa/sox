package sox.command.dispatch;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

/**
 * Handles dispatching the command to the appropriate method. Calls
 * {@link AbstractCommand#process(AbstractContext)} if no other method
 * was detected to be a better match for the provided context.
 */
public interface CommandDispatcher {
    /**
     * Clears any caches kept by the dispatcher. Useful for reloading classes,
     * as this allows the JVM to garbage collect any resources that would have been
     * referenced by this dispatcher.
     */
    void clearCaches();

    /**
     * Calls the provided command with the provided context. Implementations may
     * choose to call either the {@link AbstractCommand#process(AbstractContext) default}
     * handler method or another method they find more suitable.
     *
     * Always calls a single method on the provided command instance or throws an exception.
     *
     * @param command Command to run.
     * @param context Context of the command call.
     * @param <C> Type of the context implementation.
     * @param <T> Type of the command implementation.
     */
    <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> void dispatch(T command, C context);
}
