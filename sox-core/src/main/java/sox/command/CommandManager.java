package sox.command;

import sox.Sox;
import sox.command.argument.Arguments;
import sox.command.argument.split.StringSplitter;
import sox.command.hook.CommandHook;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandManager<M, C extends AbstractContext<C>> {
    private static final StringSplitter SPLITTER = new StringSplitter();

    private final Sox sox;
    private final Map<String, AbstractCommand<C>> commands;
    private final Map<String, String> aliases;
    private final List<CommandHook<C>> commandHooks;

    public CommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        this.sox = sox;
        this.commands = mapFactory.create();
        this.aliases = mapFactory.create();
        this.commandHooks = listFactory.create();
    }

    public CommandManager(@Nonnull Sox sox) {
        this(sox, HashMap::new, ArrayList::new);
    }

    public Sox sox() {
        return sox;
    }

    public Map<String, AbstractCommand<C>> commands() {
        return commands;
    }

    public List<CommandHook<C>> commandHooks() {
        return commandHooks;
    }

    public void process(M message, String content) {
        String[] parts = SPLITTER.rawSplit(content, 2);
        AbstractCommand<C> command = command(parts[0].toLowerCase());
        if(command == null) return;
        while(true) {
            content = parts.length == 1 ? "" : parts[1];
            parts = CommandManager.SPLITTER.rawSplit(content, 2);
            AbstractCommand<C> subcommand = command.subcommand(parts[0].toLowerCase());
            if(subcommand == null) {
                C context = createContext(message, new Arguments(CommandManager.SPLITTER.split(content), 0));
                AbstractCommand<C> finalCommand = command;
                List<CommandHook<C>> hooks = commandHooks;
                List<CommandHook<C>> commandSpecificHooks = finalCommand.hooks();
                if(!hooks.stream().allMatch(h->h.shouldRunCommand(context, finalCommand))) {
                    return;
                }
                if(!commandSpecificHooks.isEmpty() && !commandSpecificHooks
                        .stream().allMatch(h->h.shouldRunCommand(context, finalCommand))) {
                    return;
                }
                for(CommandHook<C> hook : commandSpecificHooks) {
                    hook.beforeCommand(context, finalCommand);
                }
                for(CommandHook<C> hook : hooks) {
                    hook.beforeCommand(context, finalCommand);
                }
                try {
                    finalCommand.process(context);
                    for(CommandHook<C> hook : commandSpecificHooks) {
                        hook.afterCommand(context, finalCommand);
                    }
                    for(CommandHook<C> hook : hooks) {
                        hook.afterCommand(context, finalCommand);
                    }
                } catch(Exception e) {
                    for(CommandHook<C> hook : commandSpecificHooks) {
                        if(hook.onCommandError(context, finalCommand, e)) {
                            return;
                        }
                    }
                    for(CommandHook<C> hook : hooks) {
                        if(hook.onCommandError(context, finalCommand, e)) {
                            return;
                        }
                    }
                }
                return;
            }
            command = subcommand;
        }
    }

    public void register(@Nonnull String name, @Nonnull AbstractCommand<C> command) {
        commands.put(name, command);
        command.onRegister(this, null);
    }

    public void registerAlias(@Nonnull String alias, @Nonnull String target) {
        aliases.put(alias, target);
    }

    @Nullable
    @CheckReturnValue
    public AbstractCommand<C> command(@Nonnull String name) {
        AbstractCommand<C> command = commands.get(name);
        if(command == null) {
            String alias = aliases.get(name);
            if(alias == null) return null;
            return commands.get(alias);
        }
        return command;
    }

    public abstract void register(Class<? extends AbstractCommand<C>> commandClass);

    public abstract C createContext(M message, Arguments arguments);
}
