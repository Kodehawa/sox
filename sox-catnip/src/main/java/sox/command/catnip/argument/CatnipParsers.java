package sox.command.catnip.argument;

import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.User;
import sox.command.argument.Parser;
import sox.command.argument.Parsers;
import sox.command.catnip.Context;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CatnipParsers {
    private static final Pattern MENTION_PATTERN = Pattern.compile("^<@!?(\\d{1,20})>$");

    private CatnipParsers() {}

    /**
     * Returns a parser that matches a member for the current guild.
     *
     * @return A parser that matches a guild member.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Member> member() {
        Parser<User> userParser = user();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            String guildId = c.message().guildId();
            if(guildId == null) return Optional.empty();
            return userParser.parse(c, arguments).map(u -> c.message().catnip().cache().member(guildId, u.id()));
        };
    }

    /**
     * Returns a parser that matches a discord user.
     *
     * This parser tries to find an user with the following methods, in order:
     * <ul>
     *     <li>An user ID</li>
     *     <li>An user mention</li>
     * </ul>
     *
     * @return A parser that matches a discord user.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<User> user() {
        Parser<Long> longParser = Parsers.parseLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            Optional<Long> id = longParser.parse(c, arguments);
            EntityCache cache = c.message().catnip().cache();
            if(id.isPresent()) {
                return id.map(String::valueOf).map(cache::user);
            }
            arguments.back();
            String search = arguments.next().getValue();
            Matcher mention = MENTION_PATTERN.matcher(search);
            if(mention.find()) {
                try {
                    return Optional.ofNullable(cache.user(
                            String.valueOf(Long.parseUnsignedLong(mention.group(1)))
                    ));
                } catch(NumberFormatException ignored) {}
            }
            return Optional.empty();
        };
    }
}
