package sox;

import sox.command.AbstractContext;
import sox.command.CommandManager;
import sox.command.hook.AfterCommand;
import sox.command.hook.BeforeCommand;
import sox.command.hook.CommandErrorHandler;
import sox.command.hook.CommandFilter;
import sox.command.hook.CommandHook;
import sox.impl.SoxImpl;
import sox.util.CommandManagerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <M> Type of the object given to command managers, usually the discord library's "Message" object.
 * @param <C> Type of the AbstractContext implementation specific to the used discord library.
 * @param <T> Type of the SoxBuilder subclass.
 */
public abstract class SoxBuilder<M, C extends AbstractContext<C>, T extends SoxBuilder<M, C, T>> {
    protected final List<CommandHook<C>> hooks = new ArrayList<>();
    protected boolean defaultErrorHandler = true;
    protected CommandManagerFactory<M, C> commandManagerFactory;

    protected SoxBuilder(CommandManagerFactory<M, C> factory) {
        this.commandManagerFactory = factory;
    }

    @Nonnull
    @CheckReturnValue
    public T commandManager(@Nonnull CommandManagerFactory<M, C> commandManagerFactory) {
        this.commandManagerFactory = commandManagerFactory;
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T commandFilter(CommandFilter<C> filter) {
        hooks.add(CommandHook.fromFilter(filter));
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T beforeCommands(BeforeCommand<C> before) {
        hooks.add(CommandHook.fromBefore(before));
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T afterCommands(AfterCommand<C> after) {
        hooks.add(CommandHook.fromAfter(after));
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T commandErrorHandler(CommandErrorHandler<C> errorHandler) {
        hooks.add(CommandHook.fromErrorHandler(errorHandler));
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T disableDefaultErrorHandler() {
        defaultErrorHandler = false;
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public T enableDefaultErrorHandler() {
        defaultErrorHandler = true;
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public Sox build() {
        SoxImpl<M, C> impl = newInstance();
        CommandManager<M, C> cm = commandManagerFactory.create(impl);
        cm.commandHooks().addAll(hooks);
        if(defaultErrorHandler) {
            cm.commandHooks().add(CommandHook.fromErrorHandler(CommandErrorHandler.<C>fromConsumer(Exception::printStackTrace)));
        }
        impl.registerCommandManager(cm);
        return impl;
    }

    @Nonnull
    @CheckReturnValue
    protected abstract SoxImpl<M, C> newInstance();

    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    private <U extends SoxBuilder<M, C, T>> U asActualType() {
        return (U)this;
    }
}
