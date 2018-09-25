package sox.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a weight for a constructor.
 *
 * When choosing the constructor order, the following comparisons are used, given two constructors:
 * <ul>
 *     <li>If only one has a weight, it's attempted before</li>
 *     <li>If both have weights, the one with higher weight is attempted before</li>
 *     <li>If neither have weights or both have the same weight:
 *     <ul>
 *         <li>If one has more arguments, it'll be attempted before</li>
 *         <li>Otherwise, the one with the first more specific type will be attempted before</li>
 *         <li>If no more specific constructor is found, the order between them is undefined</li>
 *     </ul>
 *     </li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface InjectWeight {
    int value();
}
