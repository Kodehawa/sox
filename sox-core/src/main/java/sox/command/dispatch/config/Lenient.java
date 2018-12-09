package sox.command.dispatch.config;

import sox.command.argument.Parsers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows lenient parsing.
 *
 * Used for
 * <ul>
 *     <li>{@link Integer integers}</li>
 *     <li>{@link Long longs}</li>
 * </ul>
 *
 * @see Parsers#lenientInt()
 * @see Parsers#lenientLong()
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lenient {}
