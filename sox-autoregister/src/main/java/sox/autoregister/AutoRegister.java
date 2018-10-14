package sox.autoregister;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import sox.Sox;
import sox.command.AbstractCommand;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Handles finding command classes and automatically registering.
 * <br>Example:
 * <pre><code>
 * Sox sox = ...
 * try(AutoRegister finder = AutoRegister.jda("my.package")) {
 *     finder.register(sox);
 * }
 * </code></pre>
 * or
 * <pre><code>
 * Sox sox = ...
 * AutoRegister.jda("my.package").into(sox);
 * </code></pre>
 */
public abstract class AutoRegister implements AutoCloseable {
    private final ScanResult result;

    public AutoRegister(@Nonnull ScanResult result) {
        this.result = result;
    }

    public AutoRegister(@Nonnull ClassGraph classGraph) {
        this(classGraph.scan());
    }

    public AutoRegister(@Nonnull String... packages) {
        this(new ClassGraph().whitelistPackages(packages));
    }

    @Nonnull
    @CheckReturnValue
    public abstract Class<? extends AbstractCommand<?, ?>> commandClass();

    public void register(Sox sox) {
        result.getSubclasses(commandClass().getName()).filter(c -> c.getOuterClasses().isEmpty())
                .filter(c -> !(c.isAbstract() || c.isSynthetic()))
                .loadClasses()
                .stream()
                .<Class<? extends AbstractCommand<?, ?>>>map(c -> c.asSubclass(commandClass()))
                .forEach(sox::registerCommand);
    }

    public void into(Sox sox) {
        try(AutoRegister __ = this) {
            register(sox);
        }
    }

    @Override
    public void close() {
        result.close();
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister jda(@Nonnull String... packages) {
        return new ForClass(jdaCommandClass(), packages);
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister jda(@Nonnull ClassGraph classGraph) {
        return new ForClass(jdaCommandClass(), classGraph);
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister jda(@Nonnull ScanResult result) {
        return new ForClass(jdaCommandClass(), result);
    }

    @Nonnull
    @CheckReturnValue
    private static Class<? extends AbstractCommand<?, ?>> jdaCommandClass() {
        return findClassOrThrow("sox.command.jda.Command");
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister catnip(@Nonnull String... packages) {
        return new ForClass(catnipCommandClass(), packages);
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister catnip(@Nonnull ClassGraph classGraph) {
        return new ForClass(catnipCommandClass(), classGraph);
    }

    @Nonnull
    @CheckReturnValue
    public static AutoRegister catnip(@Nonnull ScanResult result) {
        return new ForClass(catnipCommandClass(), result);
    }

    @Nonnull
    @CheckReturnValue
    private static Class<? extends AbstractCommand<?, ?>> catnipCommandClass() {
        return findClassOrThrow("sox.command.catnip.Command");
    }

    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    private static Class<? extends AbstractCommand<?, ?>> findClassOrThrow(@Nonnull String name) {
        try {
            return (Class<? extends AbstractCommand<?, ?>>)Class.forName(name).asSubclass(AbstractCommand.class);
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("Command class " + name + " not found");
        }
    }

    public static class ForClass extends AutoRegister {
        private final Class<? extends AbstractCommand<?, ?>> commandClass;

        public ForClass(@Nonnull Class<? extends AbstractCommand<?, ?>> commandClass, ScanResult result) {
            super(result);
            this.commandClass = commandClass;
        }

        public ForClass(@Nonnull Class<? extends AbstractCommand<?, ?>> commandClass, ClassGraph classGraph) {
            super(classGraph);
            this.commandClass = commandClass;
        }

        public ForClass(@Nonnull Class<? extends AbstractCommand<?, ?>> commandClass, String... packages) {
            super(packages);
            this.commandClass = commandClass;
        }

        @Override
        @Nonnull
        @CheckReturnValue
        public Class<? extends AbstractCommand<?, ?>> commandClass() {
            return commandClass;
        }
    }
}
