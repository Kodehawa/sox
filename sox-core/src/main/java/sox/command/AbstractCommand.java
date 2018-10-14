package sox.command;

import sox.command.hook.AfterCommand;
import sox.command.hook.BeforeCommand;
import sox.command.hook.CommandErrorHandler;
import sox.command.hook.CommandFilter;
import sox.command.hook.CommandHook;
import sox.command.meta.Alias;
import sox.command.meta.Category;
import sox.command.meta.Description;
import sox.command.meta.GuildOnly;
import sox.command.meta.Meta;
import sox.command.meta.OverrideName;
import sox.command.meta.Usage;
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

public abstract class AbstractCommand<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
    private final Map<String, T> subcommands;
    private final Map<String, String> subcommandAliases;
    private final List<CommandHook<C, T>> hooks;
    private final Map<String, String> meta;
    private final List<String> aliases;
    private final String name;
    private final String category;
    private final String description;
    private final String usage;
    private final boolean guildOnly;
    private volatile ParentReference<C, T> parent;

    public AbstractCommand(MapFactory mapFactory, ListFactory listFactory) {
        this.subcommands = mapFactory.create();
        this.subcommandAliases = mapFactory.create();
        this.hooks = listFactory.create();
        Map<String, String> meta = new HashMap<>();
        for(Meta m : getClass().getAnnotationsByType(Meta.class)) {
            if(meta.put(m.name(), m.value()) != null) {
                throw new IllegalStateException("Duplicate meta for key " + m.name());
            }
        }
        this.meta = Collections.unmodifiableMap(meta);
        List<String> aliases = new ArrayList<>();
        for(Alias alias : getClass().getAnnotationsByType(Alias.class)) {
            aliases.add(alias.value());
        }
        this.aliases = Collections.unmodifiableList(aliases);
        OverrideName name = getClass().getAnnotation(OverrideName.class);
        if(name == null || name.value().trim().isEmpty()) {
            this.name = getClass().getSimpleName().toLowerCase();
        } else {
            this.name = name.value().trim().toLowerCase();
        }
        Category category = getClass().getAnnotation(Category.class);
        this.category = category == null ? null : category.value();
        Description description = getClass().getAnnotation(Description.class);
        this.description = description == null ? null : description.value();
        Usage usage = getClass().getAnnotation(Usage.class);
        this.usage = usage == null ? null : usage.value();
        this.guildOnly = getClass().getAnnotation(GuildOnly.class) != null;
    }

    public AbstractCommand() {
        this(HashMap::new, ArrayList::new);
    }

    @Nonnull
    @CheckReturnValue
    public Map<String, T> subcommands() {
        return subcommands;
    }

    @Nonnull
    @CheckReturnValue
    public List<CommandHook<C, T>> hooks() {
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

    @CheckReturnValue
    public boolean hasMeta(@Nonnull String key) {
        return meta(key) != null;
    }

    @Nonnull
    @CheckReturnValue
    public List<String> aliases() {
        return aliases;
    }

    @Nonnull
    @CheckReturnValue
    public String name() {
        return name;
    }

    @Nullable
    @CheckReturnValue
    public String category() {
        return category;
    }

    @Nullable
    @CheckReturnValue
    public String description() {
        return description;
    }

    @Nullable
    @CheckReturnValue
    public String usage() {
        return usage;
    }

    @CheckReturnValue
    public boolean guildOnly() {
        return guildOnly;
    }

    @CheckReturnValue
    @Nullable
    public AbstractCommand<C, T> parent() {
        if(parent == null) {
            throw new IllegalStateException("Parent has not been set yet! This method can only be used after the command" +
                    " has been registered");
        }
        return parent.value;
    }

    public void addHook(@Nonnull CommandHook<C, T> hook) {
        hooks.add(hook);
    }

    public void addFilter(@Nonnull CommandFilter<C, T> filter) {
        addHook(CommandHook.fromFilter(filter));
    }

    public void addBefore(@Nonnull BeforeCommand<C, T> before) {
        addHook(CommandHook.fromBefore(before));
    }

    public void addAfter(@Nonnull AfterCommand<C, T> after) {
        addHook(CommandHook.fromAfter(after));
    }

    public void addErrorHandler(@Nonnull CommandErrorHandler<C, T> errorHandler) {
        addHook(CommandHook.fromErrorHandler(errorHandler));
    }

    @OverridingMethodsMustInvokeSuper
    public void onRegister(@Nonnull CommandManager<?, C, T> commandManager, @Nullable AbstractCommand<C, T> parent) {
        this.parent = new ParentReference<>(parent);
        for(AbstractCommand<C, T> command : subcommands.values()) {
            command.onRegister(commandManager, this);
        }
        for(String alias : aliases) {
            if(parent == null) {
                commandManager.registerAlias(alias, name);
            } else {
                parent.registerSubcommandAlias(alias, name);
            }
        }
    }

    public void registerSubcommand(@Nonnull String name, @Nonnull T subcommand) {
        subcommands.put(name, subcommand);
    }

    public void registerSubcommandAlias(@Nonnull String alias, @Nonnull String target) {
        subcommandAliases.put(alias, target);
    }

    @Nullable
    @CheckReturnValue
    public T subcommand(@Nonnull String name) {
        T subcommand = subcommands.get(name);
        if(subcommand == null) {
            String alias = subcommandAliases.get(name);
            if(alias == null) return null;
            return subcommands.get(alias);
        }
        return subcommand;
    }

    public abstract void process(C context);

    @SuppressWarnings("unchecked")
    private static <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> T cast(AbstractCommand<C, ?> command) {
        return (T)command;
    }

    private static class ParentReference<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
        final AbstractCommand<C, T> value;

        private ParentReference(AbstractCommand<C, T> value) {
            this.value = value;
        }
    }
}
