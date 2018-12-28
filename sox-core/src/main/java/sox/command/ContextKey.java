package sox.command;

/**
 * Used for adding custom properties to a {@link AbstractContext context} object.
 *
 * <br>Instances of this class should be singletons, preferably stored in
 * static final fields, as the instances are the keys to the data, and equality
 * is implemented as referential equality ({@code keyA == keyB}).
 *
 * @param <T> Type of the data stored in this key.
 */
public class ContextKey<T> {
    private final Class<T> valueClass;
    private final T defaultValue;

    public ContextKey(Class<T> valueClass, T defaultValue) {
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    public Class<T> valueClass() {
        return valueClass;
    }

    public T defaultValue() {
        return defaultValue;
    }
}
