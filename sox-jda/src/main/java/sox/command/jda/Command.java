package sox.command.jda;

import sox.command.AbstractCommand;
import sox.util.ListFactory;
import sox.util.MapFactory;

public abstract class Command extends AbstractCommand<Context> {
    public Command(MapFactory mapFactory, ListFactory listFactory) {
        super(mapFactory, listFactory);
    }

    public Command() {
        super();
    }
}
