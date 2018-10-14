package sox.command.jda;

import sox.command.AbstractCommand;
import sox.util.ListFactory;
import sox.util.MapFactory;

/**
 * Command class used for JDA bots.
 */
public abstract class Command extends AbstractCommand<Context, Command> {
    public Command(MapFactory mapFactory, ListFactory listFactory) {
        super(mapFactory, listFactory);
    }

    public Command() {
        super();
    }
}
