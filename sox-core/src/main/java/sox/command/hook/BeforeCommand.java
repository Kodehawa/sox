package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.Nonnull;

/**
 * Called before a command executes.
 *
 * @param <C> Type of the context implementation.
 * @param <T> Type of the command implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface BeforeCommand<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
    /**
     * Called before a command executes, if all filters allowed it to execute.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     */
    void beforeCommand(@Nonnull C context, @Nonnull T command);
}
