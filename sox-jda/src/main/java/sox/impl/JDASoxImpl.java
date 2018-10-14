package sox.impl;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import sox.command.CommandManager;
import sox.command.jda.Command;
import sox.command.jda.Context;
import sox.command.jda.PrefixProvider;

import java.util.Iterator;
import java.util.List;

public class JDASoxImpl extends SoxImpl<Message, Context, Command> implements EventListener {
    protected final List<PrefixProvider> prefixProviders;

    public JDASoxImpl(List<PrefixProvider> prefixProviders) {
        this.prefixProviders = prefixProviders;
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
        providers.next().getPrefix(manager.sox(), message)
                .handle((prefix, error) -> {
                    if(prefix == null || error != null) {
                        tryNextPrefix(manager, providers, message);
                        return null;
                    }
                    manager.process(message, message.getContentRaw().trim().substring(prefix.length()).trim());
                    return null;
                });
    }
}
