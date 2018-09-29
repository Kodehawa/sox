package sox.autoregister;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import sox.Sox;
import sox.command.AbstractCommand;

/**
 * Handles finding command classes and automatically registering.
 * <br>Example:
 * <pre><code>
 * Sox sox = ...
 * try(AutoRegister finder = AutoRegister.jda("my.package")) {
 *     finder.register(sox);
 * }
 * </code></pre>
 */
public abstract class AutoRegister implements AutoCloseable {
    private final ScanResult result;

    public AutoRegister(ScanResult result) {
        this.result = result;
    }

    public AutoRegister(ClassGraph classGraph) {
        this(classGraph.scan());
    }

    public AutoRegister(String... packages) {
        this(new ClassGraph().whitelistPackages(packages));
    }

    public abstract Class<? extends AbstractCommand<?>> commandClass();

    public void register(Sox sox) {
        result.getSubclasses(commandClass().getName()).filter(c -> c.getOuterClasses().isEmpty())
                .filter(c -> !(c.isAbstract() || c.isSynthetic()))
                .loadClasses()
                .stream()
                .<Class<? extends AbstractCommand<?>>>map(c -> c.asSubclass(commandClass()))
                .forEach(sox::registerCommand);
    }

    @Override
    public void close() {
        result.close();
    }

    public static AutoRegister jda() {
        return new ForClass(jdaCommandClass());
    }

    public static AutoRegister jda(String... packages) {
        return new ForClass(jdaCommandClass(), packages);
    }

    public static AutoRegister jda(ClassGraph classGraph) {
        return new ForClass(jdaCommandClass(), classGraph);
    }

    public static AutoRegister jda(ScanResult result) {
        return new ForClass(jdaCommandClass(), result);
    }

    private static Class<? extends AbstractCommand<?>> jdaCommandClass() {
        return findClassOrThrow("sox.command.jda.Command");
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends AbstractCommand<?>> findClassOrThrow(String name) {
        try {
            return (Class<? extends AbstractCommand<?>>)Class.forName(name).asSubclass(AbstractCommand.class);
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("Command class " + name + " not found");
        }
    }

    public static class ForClass extends AutoRegister {
        private final Class<? extends AbstractCommand<?>> commandClass;

        public ForClass(Class<? extends AbstractCommand<?>> commandClass, ScanResult result) {
            super(result);
            this.commandClass = commandClass;
        }

        public ForClass(Class<? extends AbstractCommand<?>> commandClass, ClassGraph classGraph) {
            super(classGraph);
            this.commandClass = commandClass;
        }

        public ForClass(Class<? extends AbstractCommand<?>> commandClass, String... packages) {
            super(packages);
            this.commandClass = commandClass;
        }

        public ForClass(Class<? extends AbstractCommand<?>> commandClass) {
            super();
            this.commandClass = commandClass;
        }

        @Override
        public Class<? extends AbstractCommand<?>> commandClass() {
            return commandClass;
        }
    }
}
