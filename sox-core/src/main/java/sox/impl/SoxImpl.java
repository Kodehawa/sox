package sox.impl;

import sox.Sox;
import sox.command.AbstractCommand;
import sox.command.AbstractContext;
import sox.command.CommandManager;
import sox.inject.Injector;
import sox.service.ServiceManager;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class SoxImpl<M, C extends AbstractContext<C>, T extends AbstractCommand<C, T>> implements Sox, Consumer<M> {
    protected final AtomicReference<CommandManager<M, C, T>> commandManagerReference = new AtomicReference<>();
    protected final ServiceManager serviceManager;
    protected final Injector injector;

    protected SoxImpl() {
        this.serviceManager = new ServiceManager();
        this.injector = new Injector(serviceManager);
        serviceManager.registerService(serviceManager);
        serviceManager.registerService(injector);
        serviceManager.registerService(this);
    }

    public void registerCommandManager(CommandManager<M, C, T> commandManager) {
        if(!commandManagerReference.compareAndSet(null, commandManager)) {
            throw new IllegalStateException("AbstractCommand manager already set!");
        }
        serviceManager.registerService(commandManager);
    }

    public void withCommandManager(Consumer<CommandManager<M, C, T>> action) {
        CommandManager<M, C, T> manager = commandManagerReference.get();
        if(manager != null) {
            action.accept(manager);
        }
    }

    @Nonnull
    @Override
    public ServiceManager serviceManager() {
        return serviceManager;
    }

    @Nonnull
    @Override
    public CommandManager commandManager() {
        CommandManager cm = commandManagerReference.get();
        if(cm == null) {
            throw new IllegalStateException("No command manager set!");
        }
        return cm;
    }

    @Nonnull
    @Override
    public Injector injector() {
        return injector;
    }
}
