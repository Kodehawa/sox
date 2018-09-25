package sox.command.hook;

import sox.command.AbstractContext;
import sox.command.AbstractCommand;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Empty command hook. Does nothing by default.
 *
 * @param <C> Type of the context implementation.
 */
public abstract class AbstractCommandHook<C extends AbstractContext<C>> implements CommandHook<C> {
    @CheckReturnValue
    public boolean shouldRunCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {
        return true;
    }

    public void afterCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {}

    public void beforeCommand(@Nonnull C context, @Nonnull AbstractCommand<C> command) {}

    @CheckReturnValue
    public boolean onCommandError(@Nonnull C context, @Nonnull AbstractCommand<C> command, @Nonnull Exception e) {
        return false;
    }
}
