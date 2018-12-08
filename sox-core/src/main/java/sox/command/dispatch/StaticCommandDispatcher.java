package sox.command.dispatch;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

public class StaticCommandDispatcher implements CommandDispatcher {
    @Override
    public void clearCaches() {
        //noop
    }

    @Override
    public <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> void dispatch(T command, C context) {
        command.process(context);
    }
}
