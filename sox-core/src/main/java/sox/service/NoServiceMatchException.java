package sox.service;

/**
 * Thrown when {@link ServiceManager#getService(Class) getService} is unable to find any matching services.
 */
public class NoServiceMatchException extends RuntimeException {
    private final Class<?> targetClass;

    public NoServiceMatchException(Class<?> targetClass) {
        super("No service implementations matching " + targetClass + " found");
        this.targetClass = targetClass;
    }

    /**
     * Class given to {@link ServiceManager#getService(Class) getService} that yielded in no results.
     *
     * @return Class being searched for.
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }
}
