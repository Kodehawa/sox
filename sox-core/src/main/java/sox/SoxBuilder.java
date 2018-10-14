package sox;

import sox.command.AbstractCommand;
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
 * @param <CT> Type of the AbstractCommand implementation specific to the used discord library.
 * @param <T> Type of the SoxBuilder subclass.
 */
public abstract class SoxBuilder<M, C extends AbstractContext<C>, CT extends AbstractCommand<C, CT>, T extends SoxBuilder<M, C, CT, T>> {
    protected final List<CommandHook<C, CT>> hooks = new ArrayList<>();
    protected boolean defaultErrorHandler = true;
    protected CommandManagerFactory<M, C, CT> commandManagerFactory;

    protected SoxBuilder(CommandManagerFactory<M, C, CT> factory) {
        this.commandManagerFactory = factory;
    }

    @Nonnull
    public T commandManager(@Nonnull CommandManagerFactory<M, C, CT> commandManagerFactory) {
        this.commandManagerFactory = commandManagerFactory;
        return asActualType();
    }

    @Nonnull
    public T commandFilter(CommandFilter<C, CT> filter) {
        hooks.add(CommandHook.fromFilter(filter));
        return asActualType();
    }

    @Nonnull
    public T beforeCommands(BeforeCommand<C, CT> before) {
        hooks.add(CommandHook.fromBefore(before));
        return asActualType();
    }

    @Nonnull
    public T afterCommands(AfterCommand<C, CT> after) {
        hooks.add(CommandHook.fromAfter(after));
        return asActualType();
    }

    @Nonnull
    public T commandErrorHandler(CommandErrorHandler<C, CT> errorHandler) {
        hooks.add(CommandHook.fromErrorHandler(errorHandler));
        return asActualType();
    }

    @Nonnull
    public T disableDefaultErrorHandler() {
        defaultErrorHandler = false;
        return asActualType();
    }

    @Nonnull
    public T enableDefaultErrorHandler() {
        defaultErrorHandler = true;
        return asActualType();
    }

    @Nonnull
    @CheckReturnValue
    public Sox build() {
        SoxImpl<M, C, CT> impl = newInstance();
        CommandManager<M, C, CT> cm = commandManagerFactory.create(impl);
        cm.commandHooks().addAll(hooks);
        if(defaultErrorHandler) {
            cm.commandHooks().add(CommandHook.fromErrorHandler(CommandErrorHandler.<C, CT>fromConsumer(Exception::printStackTrace)));
        }
        impl.registerCommandManager(cm);
        return impl;
    }

    @Nonnull
    @CheckReturnValue
    protected abstract SoxImpl<M, C, CT> newInstance();

    @SuppressWarnings("unchecked")
    @Nonnull
    @CheckReturnValue
    private <U extends SoxBuilder<M, C, CT, T>> U asActualType() {
        return (U)this;
    }
}
