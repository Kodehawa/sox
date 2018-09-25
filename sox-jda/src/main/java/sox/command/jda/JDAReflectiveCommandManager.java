package sox.command.jda;

import net.dv8tion.jda.core.entities.Message;
import sox.Sox;
import sox.command.AbstractCommand;
import sox.command.ReflectiveCommandManager;
import sox.command.argument.Arguments;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.Nonnull;

public class JDAReflectiveCommandManager extends ReflectiveCommandManager<Message, Context> {
    public JDAReflectiveCommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        super(sox, mapFactory, listFactory);
    }

    public JDAReflectiveCommandManager(@Nonnull Sox sox) {
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
