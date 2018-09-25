package sox.command.argument;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Provides default parser implementations.
 */
public class Parsers {
    private Parsers() {}

    /**
     * Returns a parser that matches everything.
     *
     * @return A parser that matches everything.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> string() {
        return new BasicParser<>(Function.identity());
    }

    /**
     * Returns a parser that matches integers.
     *
     * @return A parser that matches integers.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Integer> parseInt() {
        return new CatchingParser<>(Integer::valueOf);
    }

    /**
     * Returns a parser that matches longs.
     *
     * @return A parser that matches longs.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Long> parseLong() {
        return new CatchingParser<>(Long::valueOf);
    }

    /**
     * Returns a parser that matches integer ranges, inclusive on both ends.
     *
     * @param from First end of the range. May be either the lower or upper bound.
     * @param to Second end of the range. May be either the lower or upper bound.
     *
     * @return A parser that matches integer ranges, inclusive on both ends.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Integer> range(int from, int to) {
        int smaller = Math.min(from, to);
        int larger = Math.max(from, to);
        return parseInt().filter(n->n >= smaller && n <= larger);
    }

    /**
     * Returns a parser that matches long ranges, inclusive on both ends.
     *
     * @param from First end of the range. May be either the lower or upper bound.
     * @param to Second end of the range. May be either the lower or upper bound.
     *
     * @return A parser that matches long ranges, inclusive on both ends.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Long> range(long from, long to) {
        long smaller = Math.min(from, to);
        long larger = Math.max(from, to);
        return parseLong().filter(n->n >= smaller && n <= larger);
    }

    /**
     * Returns a parser that matches based on regular expressions.
     *
     * @param regex Regular expression to use. May not be null.
     *
     * @return A parser that matches based on regular expressions.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> matching(@Nonnull String regex) {
        return matching(Pattern.compile(regex));
    }

    /**
     * Returns a parser that matches based on regular expressions.
     *
     * @param regex Regular expression to use. May not be null.
     * @param flags Flags for the expression.
     *
     * @return A parser that matches based on regular expressions.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> matching(@Nonnull String regex, int flags) {
        return matching(Pattern.compile(regex, flags));
    }

    /**
     * Returns a parser that matches based on regular expressions.
     *
     * @param pattern Pattern to use. May not be null.
     *
     * @return A parser that matches based on regular expressions.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> matching(@Nonnull Pattern pattern) {
        return string().filter(s->pattern.matcher(s).matches());
    }

    /**
     * Returns a parser that matches enum values.
     *
     * @param enumClass Class of the enum.  May not be null.
     * @param <T> Enum type.
     *
     * @return A parser that matches enum values.
     */
    @Nonnull
    @CheckReturnValue
    public static <T extends Enum<T>> Parser<T> toEnum(@Nonnull Class<T> enumClass) {
        return new CatchingParser<>(s->Enum.valueOf(enumClass, s));
    }

    /**
     * Returns a parser that yields all the remaining content as-is.
     * <br>This method differs from {@link #remainingArguments() remainingArguments()} and
     * {@link #remainingArguments(String) remainingArguments(String)} because it preserves
     * all whitespace in the actual user input.
     *
     * @return A parser that yields all the remaining content as-is.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> remainingContent() {
        return (__, arguments) -> {
            if(!arguments.hasNext()) return Optional.empty();
            StringJoiner sj = new StringJoiner("");
            while(arguments.hasNext()) {
                sj.add(arguments.next().getRawValue());
            }
            return Optional.of(sj.toString());
        };
    }

    /**
     * Returns a parser that yields all remaining arguments as a string.
     *
     * @return A parser that yields all remaining arguments as a string.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> remainingArguments() {
        return remainingArguments(" ");
    }

    /**
     * Returns a parser that yields all remaining arguments as a string.
     *
     * @param delimiter Delimiter used when joining the strings. May not be null.
     *
     * @return A parser that yields all remaining arguments as a string.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> remainingArguments(@Nonnull String delimiter) {
        return (c, arguments) -> {
            if(!arguments.hasNext()) return Optional.empty();
            StringJoiner sj = new StringJoiner(delimiter);
            while(arguments.hasNext()) {
                sj.add(arguments.next().getValue());
            }
            return Optional.of(sj.toString());
        };
    }

    /**
     * Returns a parser that matches an URL.
     *
     * @return A parser that matches an URL.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<URL> url() {
        return new CatchingParser<>(URL::new);
    }

    /**
     * Returns a parser that matches an URL with one of the given protocols.
     *
     * @param allowedProtocols Protocols that are allowed.
     *
     * @return A parser that matches an URL with one of the given protocols.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<URL> url(Collection<String> allowedProtocols) {
        return url().filter(u->allowedProtocols.contains(u.getProtocol()));
    }

    /**
     * Returns a parser that matches an URL with one of the given protocols.
     *
     * @param allowedProtocols Protocols that are allowed.
     *
     * @return A parser that matches an URL with one of the given protocols.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<URL> url(String... allowedProtocols) {
        return url(Arrays.asList(allowedProtocols));
    }

    /**
     * Returns a parser that matches an HTTP URL.
     *
     * @return A parser that matches an HTTP URL.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<URL> httpUrl() {
        return url(Arrays.asList("http", "https"));
    }

    /**
     * Returns a parser that matches a string delimited by a given character.
     *
     * <ul>
     *     <li>If the given character is found, arguments will be read until a matching delimiter is found.</li>
     *     <li>If escaping is enabled, adding a {@literal \} character will escape a delimiter, or all whitespace until the next argument.</li>
     *     <li>If no matching delimiter is found, all the remaining arguments will be read.</li>
     * </ul>
     *
     * @param delimiter Delimiter for the match.
     * @param allowEscaping Allow escaping delimiters and whitespace with a backslash {@literal \}
     *
     * @return A parser that matches a string delimited by a given character.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<String> delimitedBy(char delimiter, boolean allowEscaping) {
        return (__, arguments) -> {
            if(!arguments.hasNext()) return Optional.empty();
            DelimiterContext context = new DelimiterContext(delimiter, allowEscaping);
            while(arguments.hasNext()) {
                if(!context.handle(arguments.next())) break;
            }
            return Optional.of(context.result());
        };
    }
}
