package sox.command.jda.argument;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import sox.command.argument.Parser;
import sox.command.argument.Parsers;
import sox.command.jda.Context;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JDAParsers {
    private static final Pattern MENTION_PATTERN = Pattern.compile("^<@!?(\\d{1,20})>$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^(.{2,32})#(\\d{4})$");

    private JDAParsers() {}

    /**
     * Returns a parser that matches a member for the current guild.
     *
     * @return A parser that matches a guild member.
     *
     * @implNote This parser uses the {@link #user(boolean) User} parser.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<Member> member() {
        //we use false because all members of the guild will be visible in the current
        //shard, so there's no point in resolving users from other shards.
        Parser<User> userParser = user(false);
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            if(c.channel().getType() != ChannelType.TEXT) return Optional.empty();
            return userParser.parse(c, arguments).map(c.guild()::getMember);
        };
    }

    /**
     * Returns a parser that matches a discord user.
     * <br>This method is equivalent to {@link #user(boolean) user(true)}.
     *
     * @return A parser that matches a discord user.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<User> user() {
        return user(true);
    }

    /**
     * Returns a parser that matches a discord user.
     *
     * This parser tries to find an user with the following methods, in order:
     * <ul>
     *     <li>An user ID</li>
     *     <li>An user mention</li>
     *     <li>An user tag (Name#discriminator), case sensitive</li>
     *     <li>An user tag (Name#discriminator), case insensitive</li>
     * </ul>
     *
     * @param useShardManager Whether or not to use the shard manager's user cache, instead of the current shards's.
     *
     * @return A parser that matches a discord user.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<User> user(boolean useShardManager) {
        Parser<Long> longParser = Parsers.strictLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            JDA jda = c.message().getJDA();
            SnowflakeCacheView<User> userCache = useShardManager ?
                    jda.asBot().getShardManager().getUserCache() : jda.getUserCache();
            Optional<Long> id = longParser.parse(c, arguments);
            if(id.isPresent()) {
                return id.map(userCache::getElementById);
            }
            arguments.back();
            String search = arguments.next().getValue();
            Matcher mention = MENTION_PATTERN.matcher(search);
            if(mention.find()) {
                try {
                    return Optional.ofNullable(userCache.getElementById(Long.parseUnsignedLong(mention.group(1))));
                } catch(NumberFormatException e) {
                    return Optional.empty();
                }
            }
            Matcher tagMatcher = TAG_PATTERN.matcher(search);
            while(!search.contains("#") || !tagMatcher.find()) {
                //37 = 32 name characters + 1 (# character) + 4 discriminator characters
                if(search.length() >= 37 || !arguments.hasNext()) {
                    return Optional.empty();
                }
                search += arguments.next().getRawValue();
                tagMatcher = TAG_PATTERN.matcher(search);
            }
            String username = tagMatcher.group(1);
            String discriminator = tagMatcher.group(2);
            Optional<User> exactName = userCache.getElementsByName(username, false)
                    .stream()
                    .filter(user -> user.getDiscriminator().equals(discriminator))
                    .findFirst();
            if(exactName.isPresent()) return exactName;
            return userCache.getElementsByName(username, true)
                    .stream()
                    .filter(user -> user.getDiscriminator().equals(discriminator))
                    .findFirst();
        };
    }
}
