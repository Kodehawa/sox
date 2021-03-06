package sox.command;

import sox.Sox;
import sox.inject.Injector;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ReflectiveCommandManager<M, C extends AbstractContext<C>, T extends AbstractCommand<C, T>> extends CommandManager<M, C, T> {
    protected final List<SubcommandFinder<C, T>> finders;

    public ReflectiveCommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        super(sox, mapFactory, listFactory);
        this.finders = listFactory.create();
        finders.add((manager, injector, command) -> Arrays.stream(command.getClass().getDeclaredClasses())
                .filter(commandClass()::isAssignableFrom)
                .filter(c->!c.isAnonymousClass())
                .filter(c->!c.isLocalClass())
                .filter(c->(!c.isMemberClass() || Modifier.isStatic(c.getModifiers())))
                .filter(c->!c.isSynthetic())
                .filter(c->!Modifier.isAbstract(c.getModifiers()))
                .map(injector::instantiate)
                .map(manager::cast)
        );
    }

    public ReflectiveCommandManager(@Nonnull Sox sox) {
        this(sox, HashMap::new, ArrayList::new);
    }

    @Override
    public void register(Class<? extends T> commandClass) {
        if(!commandClass().isAssignableFrom(commandClass)) {
            throw new IllegalArgumentException("Expected command to be a subtype of " + commandClass() + ", but got " + commandClass);
        }
        Injector injector = sox().injector();
        T command = injector.instantiate(commandClass);
        findSubCommands(command).forEach(c->{
            command.registerSubcommand(c.name(), c);
        });
        register(command.name(), command);
    }

    public void addSubcommandFinder(@Nonnull SubcommandFinder<C, T> finder) {
        finders.add(finder);
    }

    public abstract Class<? extends AbstractCommand<C, T>> commandClass();

    protected List<T> findSubCommands(T command) {
        return finders.stream()
                .flatMap(f -> f.findSubCommands(this, sox().injector(), command))
                .peek(c -> findSubCommands(c).forEach(c2->{
                    c.registerSubcommand(c2.name(), c2);
                }))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private T cast(Object object) {
        return (T)object;
    }

    public interface SubcommandFinder<C extends AbstractContext<C>, T extends AbstractCommand<C, T>> {
        Stream<T> findSubCommands(ReflectiveCommandManager<?, C, T> manager, Injector injector, T command);
    }
}
