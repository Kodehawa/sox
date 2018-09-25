package sox.command;

import sox.Sox;
import sox.inject.Injector;
import sox.util.ListFactory;
import sox.util.MapFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ReflectiveCommandManager<M, C extends AbstractContext<C>> extends CommandManager<M, C> {
    public ReflectiveCommandManager(@Nonnull Sox sox, @Nonnull MapFactory mapFactory, @Nonnull ListFactory listFactory) {
        super(sox, mapFactory, listFactory);
    }

    public ReflectiveCommandManager(@Nonnull Sox sox) {
        super(sox);
    }

    @Override
    public void register(Class<? extends AbstractCommand<C>> commandClass) {
        if(!commandClass().isAssignableFrom(commandClass)) {
            throw new IllegalArgumentException("Expected command to be a subtype of " + commandClass() + ", but got " + commandClass);
        }
        Injector injector = sox().injector();
        AbstractCommand<C> command = injector.instantiate(commandClass);
        findSubCommands(injector, commandClass(), command).forEach(c->{
            command.registerSubcommand(name(c), c);
        });
        register(name(command), command);
    }

    public abstract Class<? extends AbstractCommand<C>> commandClass();

    private static <C extends AbstractContext<C>> List<AbstractCommand<C>> findSubCommands(Injector injector, Class<? extends AbstractCommand<C>> commandClass, AbstractCommand<C> command) {
        return Arrays.stream(command.getClass().getDeclaredClasses())
                .filter(commandClass::isAssignableFrom)
                .filter(c->!c.isAnonymousClass())
                .filter(c->!c.isLocalClass())
                .filter(c->(!c.isMemberClass() || Modifier.isStatic(c.getModifiers())))
                .filter(c->!c.isSynthetic())
                .filter(c->!Modifier.isAbstract(c.getModifiers()))
                .map(injector::instantiate)
                .map(ReflectiveCommandManager::<C>cast)
                .peek(c -> findSubCommands(injector, commandClass, c).forEach(c2->{
                    c.registerSubcommand(name(c2), c2);
                }))
                .collect(Collectors.toList());
    }

    private static <C extends AbstractContext<C>> String name(AbstractCommand<C> command) {
        Class<?> commandClass = command.getClass();
        AbstractCommand.Meta meta = commandClass.getAnnotation(AbstractCommand.Meta.class);
        if(meta == null || meta.name().trim().isEmpty()) {
            return commandClass.getSimpleName().toLowerCase();
        }
        return meta.name().trim().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private static <C extends AbstractContext<C>> AbstractCommand<C> cast(Object object) {
        return (AbstractCommand<C>)object;
    }
}
