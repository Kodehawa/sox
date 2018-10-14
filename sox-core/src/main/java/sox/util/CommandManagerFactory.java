package sox.util;

import sox.Sox;
import sox.command.AbstractCommand;
import sox.command.AbstractContext;
import sox.command.CommandManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Instantiates a command manager.
 *
 * @param <M> Type of the "Message" object.
 * @param <C> Type of the {@link AbstractContext context} implementation.
 */
@FunctionalInterface
public interface CommandManagerFactory<M, C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
    /**
     * Creates a new command manager.
     *
     * @param sox Sox instance to use with the manager.
     *
     * @return A new command manager.
     */
    @Nonnull
    @CheckReturnValue
    CommandManager<M, C, T> create(@Nonnull Sox sox);
}
