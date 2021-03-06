package sox.command.catnip;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.builder.EmbedBuilder;
import com.mewna.catnip.entity.builder.MessageBuilder;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import sox.Sox;
import sox.command.AbstractContext;
import sox.command.argument.Arguments;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Context class used for Catnip bots.
 */
public class Context extends AbstractContext<Context> {
    private final Message message;

    protected Context(@Nonnull Sox sox, @Nonnull Arguments arguments, @Nonnull Message message) {
        super(sox, arguments, new HashMap<>());
        this.message = message;
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content) {
        return message.catnip().rest().channel().sendMessage(message.channelId(), content);
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull EmbedBuilder embed) {
        return send(embed.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull Embed embed) {
        return message.catnip().rest().channel().sendMessage(message.channelId(), embed);
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content, @Nonnull EmbedBuilder embed) {
        return send(content, embed.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content, @Nonnull Embed embed) {
        return send(new MessageBuilder().embed(embed).content(content));
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull MessageBuilder message) {
        return send(message.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull Message message) {
        return this.message.catnip().rest().channel().sendMessage(this.message.channelId(), message);
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public Context snapshot() {
        Context context = new Context(sox, arguments.snapshot(), message);
        if(serviceManager != null) {
            context.serviceManager = serviceManager.snapshot();
        }
        context.customProperties.putAll(customProperties);
        return context;
    }

    @CheckReturnValue
    public boolean isDM() {
        return message.guildId() == null;
    }

    @Nonnull
    @CheckReturnValue
    public Message message() {
        return message;
    }

    @Nonnull
    @CheckReturnValue
    public MessageChannel channel() {
        return message.channel();
    }

    @Nonnull
    @CheckReturnValue
    public TextChannel textChannel() {
        return message.channel().asTextChannel();
    }

    @Nonnull
    @CheckReturnValue
    public User author() {
        return message.author();
    }

    @Nonnull
    @CheckReturnValue
    public Member member() {
        return Objects.requireNonNull(message.member(), "No member present");
    }

    @Nonnull
    @CheckReturnValue
    public Guild guild() {
        return Objects.requireNonNull(message.guild(), "No guild present");
    }

    @Nonnull
    @CheckReturnValue
    public Catnip catnip() {
        return message.catnip();
    }
}
