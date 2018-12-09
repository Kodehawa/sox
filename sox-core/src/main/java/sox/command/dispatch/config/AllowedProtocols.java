package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a list of protocols to accept for parsed URLs.
 *
 * These protocols are combined with those of the {@link Http Http} annotation.
 *
 * Used for
 * <ul>
 *     <li>{@link java.net.URL URLs}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedProtocols {
    String[] protocols();
}
