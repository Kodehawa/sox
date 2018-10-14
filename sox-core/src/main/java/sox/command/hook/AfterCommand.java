package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.Nonnull;

/**
 * Called after a command executes <b>successfully</b>.
 *
 * @param <C> Type of the context implementation.
 * @param <T> Type of the command implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface AfterCommand<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
    /**
     * Called after a command executes <b>successfully</b>.
     *
     * @param context Context for the command call.
     * @param command Command called.
     */
    void afterCommand(@Nonnull C context, @Nonnull T command);
}
