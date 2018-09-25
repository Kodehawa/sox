package sox.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Instantiates maps.
 */
@FunctionalInterface
public interface MapFactory {
    /**
     * Creates a new map.
     *
     * @param <K> Type of the map keys.
     * @param <V> Type of the map values.
     *
     * @return A new map.
     */
    @Nonnull
    @CheckReturnValue
    <K, V> Map<K, V> create();
}
