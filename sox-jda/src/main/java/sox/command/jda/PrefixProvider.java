package sox.command.jda;

import net.dv8tion.jda.core.entities.Message;
import sox.Sox;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

@FunctionalInterface
public interface PrefixProvider {
    @Nonnull
    CompletionStage<String> getPrefix(@Nonnull Sox sox, @Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    static <T> PrefixProvider fromServiceBlocking(@Nonnull Class<T> serviceClass, @Nonnull BiFunction<T, Message, String> function) {
        return fromService(serviceClass, (service, message) -> CompletableFuture.completedFuture(function.apply(service, message)));
    }

    @Nonnull
    @CheckReturnValue
    static <T> PrefixProvider fromService(@Nonnull Class<T> serviceClass, @Nonnull BiFunction<T, Message, CompletionStage<String>> function) {
        return (sox, message) -> {
            Set<T> services = sox.serviceManager().findServices(serviceClass, false);
            if(services.isEmpty()) {
                services = sox.serviceManager().findServices(serviceClass, true);
                if(services.isEmpty()) return CompletableFuture.completedFuture(null);
            }
            if(services.size() != 1) return CompletableFuture.completedFuture(null);
            return function.apply(services.iterator().next(), message);
        };
    }

    @Nonnull
    @CheckReturnValue
    static PrefixProvider mention() {
        return (__, message) -> {
            String content = message.getContentRaw().trim();
            String userMention = message.getJDA().getSelfUser().getAsMention();
            if(content.startsWith(userMention)) return CompletableFuture.completedFuture(userMention);
            if(message.getGuild() == null) return CompletableFuture.completedFuture(null);
            String memberMention = message.getGuild().getSelfMember().getAsMention();
            if(content.startsWith(memberMention)) return CompletableFuture.completedFuture(memberMention);
            return CompletableFuture.completedFuture(null);
        };
    }

    @Nonnull
    @CheckReturnValue
    static PrefixProvider startingWith(@Nonnull String... options) {
        return (__, message) -> {
            String content = message.getContentRaw().trim();
            for(String s : options) {
                if(content.startsWith(s)) return CompletableFuture.completedFuture(s);
            }
            return CompletableFuture.completedFuture(null);
        };
    }
}
