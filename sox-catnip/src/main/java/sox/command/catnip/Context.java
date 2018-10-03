package sox.command.catnip;

import com.mewna.catnip.entity.Message;
import sox.Sox;
import sox.command.AbstractContext;
import sox.command.argument.Arguments;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * Context class used for Catnip bots.
 */
public class Context extends AbstractContext<Context> {
    private final Message message;

    protected Context(@Nonnull Sox sox, @Nonnull Arguments arguments, @Nonnull Message message) {
        super(sox, arguments);
        this.message = message;
    }

    @Nonnull
    public CompletionStage<Message> send(String content) {
        return message.catnip().rest().channel().sendMessage(message.channelId(), content);
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
        return message.guildId() == null;
    }

    @Nonnull
    @CheckReturnValue
    public Message message() {
        return message;
    }
}
