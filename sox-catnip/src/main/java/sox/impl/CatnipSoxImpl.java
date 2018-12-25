package sox.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.Category;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.shard.DiscordEvent;
import com.mewna.catnip.shard.EventType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import sox.command.CommandManager;
import sox.command.catnip.Command;
import sox.command.catnip.Context;
import sox.command.catnip.PrefixProvider;
import sox.command.catnip.argument.CatnipParsers;
import sox.command.dispatch.CommandDispatcher;
import sox.command.dispatch.DynamicCommandDispatcher;
import sox.command.dispatch.ParserRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CatnipSoxImpl extends SoxImpl<Message, Context, Command> implements Extension {
    protected final List<PrefixProvider> prefixProviders;
    protected final String deploymentID;

    //used for catnip extension
    private Catnip catnip;
    private Vertx vertx;
    private io.vertx.core.Context context;

    public CatnipSoxImpl(List<PrefixProvider> prefixProviders, String deploymentID) {
        this.prefixProviders = prefixProviders;
        this.deploymentID = deploymentID;
    }

    @Override
    public void registerCommandDispatcher(CommandDispatcher dispatcher) {
        if(dispatcher instanceof DynamicCommandDispatcher) {
            ParserRegistry r = ((DynamicCommandDispatcher)dispatcher).registry();
            r.register(User.class, CatnipParsers.user());
            r.register(Member.class, CatnipParsers.member());
            r.register(TextChannel.class, CatnipParsers.textChannel());
            r.register(VoiceChannel.class, CatnipParsers.voiceChannel());
            r.register(Category.class, CatnipParsers.category());
        }
        super.registerCommandDispatcher(dispatcher);
    }

    @Override
    public void accept(Message message) {
        if(message.webhookId() != null || message.author().bot()) return;
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
                    String content = message.content();
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

    @Nonnull
    @Override
    public String name() {
        return "sox";
    }

    @Nonnull
    @Override
    public Catnip catnip() {
        return catnip;
    }

    @Override
    public Extension catnip(@Nonnull Catnip catnip) {
        this.catnip = catnip;
        return this;
    }

    @Override
    public Extension registerHook(@Nonnull CatnipHook catnipHook) {
        return this;
    }

    @Override
    public Set<CatnipHook> hooks() {
        return Collections.emptySet();
    }

    @Override
    public Extension unregisterHook(@Nonnull CatnipHook catnipHook) {
        return this;
    }

    @Override
    public <T> MessageConsumer<T> on(@Nonnull EventType<T> type) {
        MessageConsumer<T> consumer = catnip().eventBus().consumer(type.key());
        context.addCloseHook(consumer::unregister);
        return consumer;
    }

    @Override
    public <T> MessageConsumer<T> on(@Nonnull EventType<T> type, @Nonnull Consumer<T> handler) {
        return on(type).handler(m -> handler.accept(m.body()));
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, io.vertx.core.Context context) {
        this.vertx = vertx;
        this.context = context;
    }

    @Override
    public String deploymentID() {
        return deploymentID;
    }

    @Override
    public void start(Future<Void> startFuture) {
        on(DiscordEvent.MESSAGE_CREATE, this);
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {}
}
