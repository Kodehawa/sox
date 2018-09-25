package sox.service;

/**
 * Thrown when {@link ServiceManager#registerService(Object, boolean) registerService} is called
 * with an instance of an already registered type and the override flag is false.
 */
public class DuplicateServiceDefinitionException extends RuntimeException {
    private final Class<?> serviceClass;

    public DuplicateServiceDefinitionException(Class<?> serviceClass) {
        super("Duplicate service definition for " + serviceClass);
        this.serviceClass = serviceClass;
    }

    /**
     * Class of the service being registered.
     *
     * @return Service class.
     */
    public Class<?> getServiceClass() {
        return serviceClass;
    }
}
