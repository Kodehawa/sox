package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures the value is inside the given range.
 *
 * Used for
 * <ul>
 *     <li>{@link Integer integers}</li>
 *     <li>{@link Long longs}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    long from();
    long to();
}
