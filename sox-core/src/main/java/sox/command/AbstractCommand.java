package sox.command;

import sox.command.hook.AfterCommand;
import sox.command.hook.BeforeCommand;
import sox.command.hook.CommandErrorHandler;
import sox.command.hook.CommandFilter;
import sox.command.hook.CommandHook;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand<C extends AbstractContext<C>> {
    private final Map<String, AbstractCommand<C>> subcommands;
    private final List<CommandHook<C>> hooks;

    public AbstractCommand(MapFactory mapFactory, ListFactory listFactory) {
        this.subcommands = mapFactory.create();
        this.hooks = listFactory.create();
    }

    public AbstractCommand() {
        this(HashMap::new, ArrayList::new);
    }

    public Map<String, AbstractCommand<C>> subcommands() {
        return subcommands;
    }

    public List<CommandHook<C>> hooks() {
        return hooks;
    }

    public void addHook(@Nonnull CommandHook<C> hook) {
        hooks.add(hook);
    }

    public void addFilter(@Nonnull CommandFilter<C> filter) {
        addHook(CommandHook.fromFilter(filter));
    }

    public void addBefore(@Nonnull BeforeCommand<C> before) {
        addHook(CommandHook.fromBefore(before));
    }

    public void addAfter(@Nonnull AfterCommand<C> after) {
        addHook(CommandHook.fromAfter(after));
    }

    public void addErrorHandler(@Nonnull CommandErrorHandler<C> errorHandler) {
        addHook(CommandHook.fromErrorHandler(errorHandler));
    }

    @OverridingMethodsMustInvokeSuper
    public void onRegister(@Nonnull CommandManager commandManager, @Nullable AbstractCommand parent) {
        for(AbstractCommand command : subcommands.values()) {
            command.onRegister(commandManager, this);
        }
    }

    public void registerSubcommand(String name, AbstractCommand<C> subcommand) {
        subcommands.put(name, subcommand);
    }

    public abstract void process(C context);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Meta {
        String name();
    }
}
