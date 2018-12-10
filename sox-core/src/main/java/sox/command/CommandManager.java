package sox.command;

import sox.Sox;
import sox.command.argument.Arguments;
import sox.command.argument.split.StringSplitter;
import sox.command.dispatch.CommandDispatcher;
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
import java.util.concurrent.atomic.AtomicReference;

public abstract class CommandManager<M, C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
    private static final StringSplitter SPLITTER = new StringSplitter();

    private final AtomicReference<UnmatchedCommandHandler<M>> unmatchedCommandHandlerReference = new AtomicReference<>();
    private final Sox sox;
    private final CommandDispatcher dispatcher;
    private final Map<String, T> commands;
    private final Map<String, String> aliases;
    private final List<CommandHook<C, T>> commandHooks;

    public CommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        this.sox = sox;
        this.dispatcher = sox.dispatcher();
        this.commands = mapFactory.create();
        this.aliases = mapFactory.create();
        this.commandHooks = listFactory.create();
    }

    public CommandManager(@Nonnull Sox sox) {
        this(sox, HashMap::new, ArrayList::new);
    }

    public UnmatchedCommandHandler<M> unmatchedCommandHandler() {
        return unmatchedCommandHandlerReference.get();
    }

    public Sox sox() {
        return sox;
    }

    public Map<String, T> commands() {
        return commands;
    }

    public List<CommandHook<C, T>> commandHooks() {
        return commandHooks;
    }

    public void setUnmatchedCommandHandler(UnmatchedCommandHandler<M> handler, boolean override) {
        if(override) {
            unmatchedCommandHandlerReference.set(handler);
        } else {
            unmatchedCommandHandlerReference.compareAndSet(null, handler);
        }
    }

    public void process(M message, String content) {
        String[] parts = SPLITTER.rawSplit(content, 2);
        T command = command(parts[0].toLowerCase());
        if(command == null) {
            UnmatchedCommandHandler<M> h = unmatchedCommandHandlerReference.get();
            if(h != null) {
                h.handleUnmatchedCommand(message);
            }
            return;
        }
        while(true) {
            content = parts.length == 1 ? "" : parts[1];
            parts = CommandManager.SPLITTER.rawSplit(content, 2);
            T subcommand = command.subcommand(parts[0].toLowerCase());
            if(subcommand == null) {
                C context = createContext(message, new Arguments(CommandManager.SPLITTER.split(content), 0));
                T finalCommand = command;
                List<CommandHook<C, T>> hooks = commandHooks;
                List<CommandHook<C, T>> commandSpecificHooks = finalCommand.hooks();
                if(!hooks.stream().allMatch(h->h.shouldRunCommand(context, finalCommand))) {
                    return;
                }
                if(!commandSpecificHooks.isEmpty() && !commandSpecificHooks
                        .stream().allMatch(h->h.shouldRunCommand(context, finalCommand))) {
                    return;
                }
                for(CommandHook<C, T> hook : commandSpecificHooks) {
                    hook.beforeCommand(context, finalCommand);
                }
                for(CommandHook<C, T> hook : hooks) {
                    hook.beforeCommand(context, finalCommand);
                }
                try {
                    dispatcher.dispatch(finalCommand, context);
                    for(CommandHook<C, T> hook : commandSpecificHooks) {
                        hook.afterCommand(context, finalCommand);
                    }
                    for(CommandHook<C, T> hook : hooks) {
                        hook.afterCommand(context, finalCommand);
                    }
                } catch(Exception e) {
                    for(CommandHook<C, T> hook : commandSpecificHooks) {
                        if(hook.onCommandError(context, finalCommand, e)) {
                            return;
                        }
                    }
                    for(CommandHook<C, T> hook : hooks) {
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

    public void register(@Nonnull String name, @Nonnull T command) {
        commands.put(name, command);
        command.onRegister(this, null);
    }

    public void registerAlias(@Nonnull String alias, @Nonnull String target) {
        aliases.put(alias, target);
    }

    @Nullable
    @CheckReturnValue
    public T command(@Nonnull String name) {
        T command = commands.get(name);
        if(command == null) {
            String alias = aliases.get(name);
            if(alias == null) return null;
            return commands.get(alias);
        }
        return command;
    }

    public abstract void register(Class<? extends T> commandClass);

    public abstract C createContext(M message, Arguments arguments);
}
