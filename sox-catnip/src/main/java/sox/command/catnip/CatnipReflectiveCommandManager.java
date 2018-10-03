package sox.command.catnip;

import com.mewna.catnip.entity.Message;
import sox.Sox;
import sox.command.AbstractCommand;
import sox.command.ReflectiveCommandManager;
import sox.command.argument.Arguments;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.Nonnull;

/**
 * Default command manager for JDA bots.
 */
public class CatnipReflectiveCommandManager extends ReflectiveCommandManager<Message, Context> {
    public CatnipReflectiveCommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        super(sox, mapFactory, listFactory);
    }

    public CatnipReflectiveCommandManager(@Nonnull Sox sox) {
        super(sox);
    }

    @Override
    public Class<? extends AbstractCommand<Context>> commandClass() {
        return Command.class;
    }

    @Override
    public Context createContext(Message message, Arguments arguments) {
        return new Context(sox(), arguments, message);
    }
}
