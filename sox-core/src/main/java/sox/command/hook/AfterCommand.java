package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.Nonnull;

/**
 * Called after a command executes <b>successfully</b>.
 *
 * @param <C> Type of the context implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface AfterCommand<C extends AbstractContext<C>> {
    /**
     * Called after a command executes <b>successfully</b>.
     *
     * @param context Context for the command call.
     * @param command Command called.
     */
    void afterCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command);
}
