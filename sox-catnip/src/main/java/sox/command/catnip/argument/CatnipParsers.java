package sox.command.catnip.argument;

import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.channel.Category;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.user.User;
import sox.command.argument.Parser;
import sox.command.argument.Parsers;
import sox.command.catnip.Context;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CatnipParsers {
    private static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("^<#(\\d{1,20})>$");
    private static final Pattern MENTION_PATTERN = Pattern.compile("^<@!?(\\d{1,20})>$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^(.{2,32})#(\\d{4})$");

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
     *     <li>An user tag (Name#discriminator), case sensitive</li>
     *     <li>An user tag (Name#discriminator), case insensitive</li>
     * </ul>
     *
     * @return A parser that matches a discord user.
     */
    @Nonnull
    @CheckReturnValue
    public static Parser<User> user() {
        Parser<Long> longParser = Parsers.strictLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            Optional<Long> id = longParser.parse(c, arguments);
            NamedCacheView<User> cache = c.message().catnip().cache().users();
            if(id.isPresent()) {
                return id.map(cache::getById);
            }
            arguments.back();
            String search = arguments.next().getValue();
            Matcher mention = MENTION_PATTERN.matcher(search);
            if(mention.find()) {
                try {
                    return Optional.ofNullable(cache.getById(
                            Long.parseUnsignedLong(mention.group(1))
                    ));
                } catch(NumberFormatException ignored) {}
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
            Collection<User> nameEquals = cache
                    .find(u -> u.username().equals(username) && u.discriminator().equals(discriminator));
            if(!nameEquals.isEmpty()) {
                return Optional.of(nameEquals.iterator().next());
            }
            Collection<User> nameEqualsIgnoreCase = cache
                    .find(u -> u.username().equalsIgnoreCase(username) && u.discriminator().equals(discriminator));
            if(!nameEqualsIgnoreCase.isEmpty()) {
                return Optional.of(nameEqualsIgnoreCase.iterator().next());
            }
            return Optional.empty();
        };
    }

    /**
     * Returns a parser that matches a text channel of the current guild.
     *
     * This parser tries to find a channel with the following methods, in order:
     * <ul>
     *     <li>A channel ID</li>
     *     <li>A channel mention</li>
     *     <li>A channel name, case sensitive</li>
     *     <li>A channel name, case insensitive</li>
     * </ul>
     *
     * @return A parser that matches a text channel of the current guild.
     */
    public static Parser<TextChannel> textChannel() {
        Parser<Long> longParser = Parsers.strictLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            MessageChannel channel = c.message().channel();
            if(channel == null || !channel.isGuild()) return Optional.empty();
            NamedCacheView<GuildChannel> channelCache = c.catnip().cache().channels(channel.asGuildChannel().guildId());
            if(channelCache.size() == 0) return Optional.empty();
            Optional<Long> id = longParser.parse(c, arguments);
            if(id.isPresent()) {
                return id.map(channelCache::getById)
                        .filter(GuildChannel::isText).map(GuildChannel::asTextChannel);
            }
            arguments.back();
            String search = arguments.next().getValue();
            Matcher mention = CHANNEL_MENTION_PATTERN.matcher(search);
            if(mention.find()) {
                try {
                    return Optional.ofNullable(channelCache.getById(Long.parseUnsignedLong(mention.group(1))))
                            .filter(GuildChannel::isText).map(GuildChannel::asTextChannel);
                } catch(NumberFormatException e) {
                    return Optional.empty();
                }
            }
            int longest = channelCache.stream()
                    .filter(GuildChannel::isText)
                    .mapToInt(tc -> tc.name().length())
                    .max()
                    .orElseThrow(IllegalStateException::new);
            while(search.length() < longest) {
                String finalSearch = search;
                Collection<GuildChannel> channels = channelCache.find(ch -> ch.isText() && ch.name().equals(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asTextChannel());
                }
                channels = channelCache.find(ch -> ch.isText() && ch.name().equalsIgnoreCase(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asTextChannel());
                }
                search += arguments.next().getRawValue();
            }
            return Optional.empty();
        };
    }

    /**
     * Returns a parser that matches a voice channel of the current guild.
     *
     * This parser tries to find a channel with the following methods, in order:
     * <ul>
     *     <li>A channel ID</li>
     *     <li>A channel name, case sensitive</li>
     *     <li>A channel name, case insensitive</li>
     * </ul>
     *
     * @return A parser that matches a voice channel of the current guild.
     */
    public static Parser<VoiceChannel> voiceChannel() {
        Parser<Long> longParser = Parsers.strictLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            MessageChannel channel = c.message().channel();
            if(channel == null || !channel.isGuild()) return Optional.empty();
            NamedCacheView<GuildChannel> channelCache = c.catnip().cache().channels(channel.asGuildChannel().guildId());
            if(channelCache.size() == 0) return Optional.empty();
            Optional<Long> id = longParser.parse(c, arguments);
            if(id.isPresent()) {
                return id.map(channelCache::getById)
                        .filter(GuildChannel::isVoice).map(GuildChannel::asVoiceChannel);
            }
            arguments.back();
            String search = arguments.next().getValue();
            int longest = channelCache.stream()
                    .filter(GuildChannel::isVoice)
                    .mapToInt(tc -> tc.name().length())
                    .max()
                    .orElseThrow(IllegalStateException::new);
            while(search.length() < longest) {
                String finalSearch = search;
                Collection<GuildChannel> channels = channelCache.find(ch -> ch.isVoice() && ch.name().equals(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asVoiceChannel());
                }
                channels = channelCache.find(ch -> ch.isVoice() && ch.name().equalsIgnoreCase(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asVoiceChannel());
                }
                search += arguments.next().getRawValue();
            }
            return Optional.empty();
        };
    }

    /**
     * Returns a parser that matches a category of the current guild.
     *
     * This parser tries to find a channel with the following methods, in order:
     * <ul>
     *     <li>A category ID</li>
     *     <li>A category name, case sensitive</li>
     *     <li>A category name, case insensitive</li>
     * </ul>
     *
     * @return A parser that matches a category of the current guild.
     */
    public static Parser<Category> category() {
        Parser<Long> longParser = Parsers.strictLong();
        return (abstractContext, arguments) -> {
            Context c = (Context)abstractContext;
            MessageChannel channel = c.message().channel();
            if(channel == null || !channel.isGuild()) return Optional.empty();
            NamedCacheView<GuildChannel> channelCache = c.catnip().cache().channels(channel.asGuildChannel().guildId());
            if(channelCache.size() == 0) return Optional.empty();
            Optional<Long> id = longParser.parse(c, arguments);
            if(id.isPresent()) {
                return id.map(channelCache::getById)
                        .filter(GuildChannel::isCategory).map(GuildChannel::asCategory);
            }
            arguments.back();
            String search = arguments.next().getValue();
            int longest = channelCache.stream()
                    .filter(GuildChannel::isCategory)
                    .mapToInt(tc -> tc.name().length())
                    .max()
                    .orElseThrow(IllegalStateException::new);
            while(search.length() < longest) {
                String finalSearch = search;
                Collection<GuildChannel> channels = channelCache.find(ch -> ch.isCategory() && ch.name().equals(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asCategory());
                }
                channels = channelCache.find(ch -> ch.isCategory() && ch.name().equalsIgnoreCase(finalSearch));
                if(!channels.isEmpty()) {
                    return Optional.of(channels.iterator().next().asCategory());
                }
                search += arguments.next().getRawValue();
            }
            return Optional.empty();
        };
    }
}
