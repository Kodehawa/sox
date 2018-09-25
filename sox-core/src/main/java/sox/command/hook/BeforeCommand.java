package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.Nonnull;

/**
 * Called before a command executes.
 *
 * @param <C> Type of the context implementation.
 *
 * @see CommandHook
 */
@FunctionalInterface
public interface BeforeCommand<C extends AbstractContext<C>> {
    /**
     * Called before a command executes, if all filters allowed it to execute.
     *
     * @param context Context for the command call.
     * @param command Command being called.
     */
    void beforeCommand(@Nonnull AbstractContext<C> context, @Nonnull AbstractCommand<C> command);
}
