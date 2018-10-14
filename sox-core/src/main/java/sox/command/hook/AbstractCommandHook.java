package sox.command.hook;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Empty command hook. Does nothing by default.
 *
 * @param <C> Type of the context implementation.
 */
public abstract class AbstractCommandHook<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> implements CommandHook<C, T> {
    @CheckReturnValue
    public boolean shouldRunCommand(@Nonnull C context, @Nonnull T command) {
        return true;
    }

    public void afterCommand(@Nonnull C context, @Nonnull T command) {}

    public void beforeCommand(@Nonnull C context, @Nonnull T command) {}

    @CheckReturnValue
    public boolean onCommandError(@Nonnull C context, @Nonnull T command, @Nonnull Exception e) {
        return false;
    }
}
