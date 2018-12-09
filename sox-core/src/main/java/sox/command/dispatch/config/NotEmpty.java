package sox.command.dispatch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures the result is not empty.
 *
 * Used for
 * <ul>
 *     <li>{@link java.util.Set sets}</li>
 *     <li>{@link java.util.List lists}</li>
 *     <li>{@link java.util.Collection collections}</li>
 *     <li>{@link java.lang.Iterable iterables}</li>
 *     <li>{@link java.util.stream.Stream streams}</li>
 *     <li>{@link java.util.Spliterator spliterators}</li>
 *     <li>{@link java.util.Iterator iterators}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmpty {}
