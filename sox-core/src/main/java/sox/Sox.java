package sox;

import sox.command.AbstractCommand;
import sox.command.CommandManager;
import sox.command.dispatch.CommandDispatcher;
import sox.inject.Injector;
import sox.service.ServiceManager;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface Sox {
    /**
     * Returns the {@link ServiceManager service manager} associated with this object.
     *
     * @return The service manager.
     */
    @Nonnull
    @CheckReturnValue
    ServiceManager serviceManager();

    /**
     * Returns the {@link CommandManager command manager} associated with this object.
     *
     * @return The command manager.
     */
    @Nonnull
    @CheckReturnValue
    CommandManager<?, ?, ?> commandManager();

    /**
     * Returns the {@link Injector injector} instance used for dependency injection.
     *
     * @return The injector.
     */
    @Nonnull
    @CheckReturnValue
    Injector injector();

    @Nonnull
    @CheckReturnValue
    CommandDispatcher dispatcher();

    /**
     * Registers a command by class.
     *
     * @param commandClass Command class to register.
     */
    @SuppressWarnings("unchecked")
    default void registerCommand(@Nonnull Class<? extends AbstractCommand<?, ?>> commandClass) {
        ((CommandManager)commandManager()).register(commandClass);
    }

    /**
     * Instantiates an object using the built-in dependency injector.
     *
     * @param objectClass Class to instantiate.
     * @param <T> Type of the wanted object.
     *
     * @return An instance of the provided class.
     *
     * @see Injector#instantiate(Class)
     */
    @Nonnull
    @CheckReturnValue
    default <T> T instantiate(@Nonnull Class<T> objectClass) {
        return injector().instantiate(objectClass);
    }
}
