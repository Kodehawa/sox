package sox.command.hook;

import sox.command.AbstractContext;
import sox.command.AbstractCommand;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Filters whether or not a command call should be allowed.
 *
 * @param <C> Type of the context implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface CommandFilter<C extends AbstractContext<C>> {
    /**
     * Filters whether or not a command call should be allowed. If any hook returns false, the command
     * call is aborted.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     *
     * @return False if the command execution should be aborted.
     */
    @CheckReturnValue
    boolean shouldRunCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command);
}
