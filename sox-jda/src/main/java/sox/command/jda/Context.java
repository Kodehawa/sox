package sox.command.jda;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import sox.Sox;
import sox.command.AbstractContext;
import sox.command.argument.Arguments;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

/**
 * Context class used for JDA bots.
 */
public class Context extends AbstractContext<Context> {
    private final Message message;

    protected Context(@Nonnull Sox sox, @Nonnull Arguments arguments, @Nonnull Message message) {
        super(sox, arguments);
        this.message = message;
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content) {
        return channel().sendMessage(content).submit();
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull EmbedBuilder embed) {
        return send(embed.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull MessageEmbed embed) {
        return channel().sendMessage(embed).submit();
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content, @Nonnull EmbedBuilder embed) {
        return send(content, embed.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull String content, @Nonnull MessageEmbed embed) {
        return send(new MessageBuilder().setContent(content).setEmbed(embed));
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull MessageBuilder message) {
        return send(message.build());
    }

    @Nonnull
    public CompletionStage<Message> send(@Nonnull Message message) {
        return channel().sendMessage(message).submit();
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public Context snapshot() {
        Context context = new Context(sox, arguments.snapshot(), message);
        if(serviceManager != null) {
            context.serviceManager = serviceManager.snapshot();
        }
        return context;
    }

    @CheckReturnValue
    public boolean isDM() {
        return channel().getType() != ChannelType.TEXT;
    }

    @Nonnull
    @CheckReturnValue
    public Message message() {
        return message;
    }

    @Nonnull
    @CheckReturnValue
    public MessageChannel channel() {
        return message.getChannel();
    }

    @Nonnull
    @CheckReturnValue
    public User author() {
        return message.getAuthor();
    }

    @Nullable
    @CheckReturnValue
    public Member member() {
        return message.getMember();
    }

    @Nonnull
    @CheckReturnValue
    public Guild guild() {
        return message.getGuild();
    }

    @Nonnull
    @CheckReturnValue
    public TextChannel textChannel() {
        return message.getTextChannel();
    }

    @Nonnull
    @CheckReturnValue
    public JDA jda() {
        return message.getJDA();
    }
}
