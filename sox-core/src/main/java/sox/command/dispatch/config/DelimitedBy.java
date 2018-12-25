package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Returns a string delimited by the provided delimiter.
 *
 * When more than one is present, the priority order is
 * <ul>
 *     <li>{@link Matching match}</li>
 *     <li>{@link DelimitedBy delimited by}</li>
 *     <li>{@link Joining joining}</li>
 *     <li>{@link RemainingContent remaining}</li>
 * </ul>
 * The highest priority found (upper on the list) will be used. The others are ignored.
 *
 * Used for
 * <ul>
 *     <li>{@link String strings}</li>
 * </ul>
 *
 * @see sox.command.argument.Parsers#delimitedBy(char, boolean)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DelimitedBy {
    char delimiter();
    boolean allowEscaping() default true;
}
