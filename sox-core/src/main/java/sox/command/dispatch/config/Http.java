package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the http and https protocols to be accepted.
 *
 * These protocols are combined with any specified in {@link AllowedProtocols AllowedProtocols}
 * annotations.
 *
 * Used for
 * <ul>
 *     <li>{@link java.net.URL URLs}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Http {}
