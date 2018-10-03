package sox.command.catnip;

import sox.command.AbstractCommand;
import sox.util.ListFactory;
import sox.util.MapFactory;

/**
 * Command class used for catnip bots.
 */
public abstract class Command extends AbstractCommand<Context> {
    public Command(MapFactory mapFactory, ListFactory listFactory) {
        super(mapFactory, listFactory);
    }

    public Command() {
        super();
    }
}
