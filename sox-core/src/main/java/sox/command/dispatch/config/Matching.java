package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures the string matches the provided pattern.
 *
 * When more than one is present, the priority order is
 * <ul>
 *     <li>{@link Matching match}</li>
 *     <li>{@link Joining joining}</li>
 *     <li>{@link RemainingContent remaining}</li>
 * </ul>
 * The highest priority found (upper on the list) will be used. The others are ignored.
 *
 * Used for
 * <ul>
 *     <li>{@link String strings}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Matching {
    String pattern();
    int flags() default 0;
}
