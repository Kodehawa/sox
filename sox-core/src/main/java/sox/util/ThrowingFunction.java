package sox.util;

/**
 * Represents a function that may throw an exception.
 *
 * @param <T> Input given to the function.
 * @param <U> Output of the function.
 */
@FunctionalInterface
public interface ThrowingFunction<T, U> {
    /**
     * Applies the function to a given argument.
     *
     * @param t Input to the function.
     *
     * @return Result of the function.
     *
     * @throws Exception If an exception happens computing the result.
     */
    U apply(T t) throws Exception;
}
