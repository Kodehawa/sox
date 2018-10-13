package sox.command;

import sox.Sox;
import sox.command.meta.OverrideName;
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

public abstract class ReflectiveCommandManager<M, C extends AbstractContext<C>> extends CommandManager<M, C> {
    protected final List<SubcommandFinder<C>> finders;

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
    public void register(Class<? extends AbstractCommand<C>> commandClass) {
        if(!commandClass().isAssignableFrom(commandClass)) {
            throw new IllegalArgumentException("Expected command to be a subtype of " + commandClass() + ", but got " + commandClass);
        }
        Injector injector = sox().injector();
        AbstractCommand<C> command = injector.instantiate(commandClass);
        findSubCommands(command).forEach(c->{
            command.registerSubcommand(name(c), c);
        });
        register(name(command), command);
    }

    public void addSubcommandFinder(@Nonnull SubcommandFinder<C> finder) {
        finders.add(finder);
    }

    public abstract Class<? extends AbstractCommand<C>> commandClass();

    protected List<AbstractCommand<C>> findSubCommands(AbstractCommand<? extends C> command) {
        return finders.stream()
                .flatMap(f -> f.findSubCommands(this, sox().injector(), command))
                .peek(c -> findSubCommands(c).forEach(c2->{
                    c.registerSubcommand(name(c2), c2);
                }))
                .collect(Collectors.toList());
    }

    private static String name(AbstractCommand<?> command) {
        Class<?> commandClass = command.getClass();
        OverrideName name = commandClass.getAnnotation(OverrideName.class);
        String n;
        if(name == null || name.value().trim().isEmpty()) {
            n = commandClass.getSimpleName().toLowerCase();
        } else {
            n = name.value().trim().toLowerCase();
        }
        //add name so command aliases can be set on registration
        command.name(n);
        return n;
    }

    @SuppressWarnings("unchecked")
    private AbstractCommand<C> cast(Object object) {
        return (AbstractCommand<C>)object;
    }

    public interface SubcommandFinder<C extends AbstractContext<C>> {
        Stream<AbstractCommand<C>> findSubCommands(ReflectiveCommandManager<?, C> manager, Injector injector, AbstractCommand<? extends C> command);
    }
}
