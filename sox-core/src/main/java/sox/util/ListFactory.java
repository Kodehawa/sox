package sox.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Instantiates lists.
 */
@FunctionalInterface
public interface ListFactory {
    /**
     * Creates a new list.
     *
     * @param <T> Type of the list.
     *
     * @return A new list.
     */
    @Nonnull
    @CheckReturnValue
    <T> List<T> create();
}
