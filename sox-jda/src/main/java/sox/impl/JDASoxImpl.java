package sox.impl;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import sox.command.CommandManager;
import sox.command.dispatch.CommandDispatcher;
import sox.command.dispatch.DynamicCommandDispatcher;
import sox.command.dispatch.ParserRegistry;
import sox.command.jda.Command;
import sox.command.jda.Context;
import sox.command.jda.PrefixProvider;
import sox.command.jda.argument.JDAParsers;
import sox.command.jda.dispatch.config.CurrentShard;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

public class JDASoxImpl extends SoxImpl<Message, Context, Command> implements EventListener {
    protected final List<PrefixProvider> prefixProviders;

    public JDASoxImpl(List<PrefixProvider> prefixProviders) {
        this.prefixProviders = prefixProviders;
    }

    @Override
    public void registerCommandDispatcher(CommandDispatcher dispatcher) {
        if(dispatcher instanceof DynamicCommandDispatcher) {
            ParserRegistry r = ((DynamicCommandDispatcher)dispatcher).registry();
            r.register(User.class, (__1, __2, annotations) -> {
                for(Annotation a : annotations) {
                    if(a instanceof CurrentShard) return JDAParsers.user(false);
                }
                return JDAParsers.user(true);
            });
            r.register(Member.class, JDAParsers.member());
        }
        super.registerCommandDispatcher(dispatcher);
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof MessageReceivedEvent) {
            accept(((MessageReceivedEvent)event).getMessage());
        }
    }

    @Override
    public void accept(Message message) {
        if(message.isWebhookMessage() || message.getAuthor().isBot()) return;
        withCommandManager(cm -> {
            Iterator<PrefixProvider> it = prefixProviders.iterator();
            tryNextPrefix(cm, it, message);
        });
    }

    private static void tryNextPrefix(CommandManager<Message, Context, Command> manager, Iterator<PrefixProvider> providers, Message message) {
        if(!providers.hasNext()) return;
        providers.next().getPrefixes(manager.sox(), message)
                .handle((prefixes, error) -> {
                    if(prefixes.isEmpty() || error != null) {
                        tryNextPrefix(manager, providers, message);
                        return null;
                    }
                    String content = message.getContentRaw();
                    for(String prefix : prefixes) {
                        if(content.startsWith(prefix)) {
                            manager.process(message, content.substring(prefix.length()).trim());
                            return null;
                        }
                    }
                    tryNextPrefix(manager, providers, message);
                    return null;
                });
    }
}
