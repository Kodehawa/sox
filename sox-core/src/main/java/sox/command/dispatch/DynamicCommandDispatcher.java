package sox.command.dispatch;

import sox.command.AbstractCommand;
import sox.command.AbstractContext;
import sox.util.MapFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicCommandDispatcher implements CommandDispatcher {
    private final Map<Class<?>, DispatchMetadata> metadata;
    private final ParserRegistry registry = new ParserRegistry();

    public DynamicCommandDispatcher(MapFactory factory) {
        this.metadata = factory.create();
    }

    public DynamicCommandDispatcher() {
        this(ConcurrentHashMap::new);
    }

    public ParserRegistry registry() {
        return registry;
    }

    @Override
    public void clearCaches() {
        metadata.clear();
    }

    @Override
    public <C extends AbstractContext<C>, T extends AbstractCommand<C, T>> void dispatch(T command, C context) {
        metadata.computeIfAbsent(command.getClass(), c -> new DispatchMetadata(registry, c))
                .dispatch(command, context);
    }
}
