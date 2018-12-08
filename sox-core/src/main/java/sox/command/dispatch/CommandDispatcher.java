package sox.command.dispatch;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;

public interface CommandDispatcher {
    void clearCaches();

    <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> void dispatch(T command, C context);
}
