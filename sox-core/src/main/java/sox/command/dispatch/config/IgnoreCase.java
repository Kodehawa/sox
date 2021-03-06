package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that casing should be ignored when parsing.
 *
 * Used for
 * <ul>
 *     <li>{@link Enum enum types}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreCase {}
