package sox.service;

/**
 * Thrown when {@link ServiceManager#getService(Class) getService} finds multiple matching classes.
 */
public class MultipleServiceMatchException extends RuntimeException {
    private final Class<?> targetClass;

    public MultipleServiceMatchException(Class<?> targetClass) {
        super("Multiple service implementations matching " + targetClass + " found");
        this.targetClass = targetClass;
    }

    /**
     * Class given to {@link ServiceManager#getService(Class) getService} that yielded in multiple results.
     *
     * @return Class being searched for.
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }
}
