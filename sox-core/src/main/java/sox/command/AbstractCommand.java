package sox.command;

import sox.command.hook.AfterCommand;
import sox.command.hook.BeforeCommand;
import sox.command.hook.CommandErrorHandler;
import sox.command.hook.CommandFilter;
import sox.command.hook.CommandHook;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand<C extends AbstractContext<C>> {
    private final Map<String, AbstractCommand<C>> subcommands;
    private final List<CommandHook<C>> hooks;
    private final Map<String, String> meta;
    private final String category;

    public AbstractCommand(MapFactory mapFactory, ListFactory listFactory) {
        this.subcommands = mapFactory.create();
        this.hooks = listFactory.create();
        Map<String, String> meta = new HashMap<>();
        for(Meta m : getClass().getAnnotationsByType(Meta.class)) {
            if(meta.put(m.name(), m.value()) != null) {
                throw new IllegalStateException("Duplicate meta for key " + m.name());
            }
        }
        this.meta = Collections.unmodifiableMap(meta);
        Category category = getClass().getAnnotation(Category.class);
        this.category = category == null ? null : category.value();
    }

    public AbstractCommand() {
        this(HashMap::new, ArrayList::new);
    }

    @Nonnull
    @CheckReturnValue
    public Map<String, AbstractCommand<C>> subcommands() {
        return subcommands;
    }

    @Nonnull
    @CheckReturnValue
    public List<CommandHook<C>> hooks() {
        return hooks;
    }

    @Nonnull
    @CheckReturnValue
    public Map<String, String> meta() {
        return meta;
    }

    @Nullable
    @CheckReturnValue
    public String meta(@Nonnull String key) {
        return meta.get(key);
    }

    @Nullable
    @CheckReturnValue
    public String category() {
        return category;
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
}
