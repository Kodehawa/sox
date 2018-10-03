package sox.command.catnip;

import com.mewna.catnip.entity.Message;
import sox.Sox;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
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
        return (sox, message) -> {
            Set<SelfInfoHolder> set = sox.serviceManager().findServices(SelfInfoHolder.class, false);
            if(set.isEmpty()) {
                SelfInfoHolder s = new SelfInfoHolder();
                set = Collections.singleton(s);
                sox.serviceManager().registerService(s);
            }
            SelfInfoHolder holder = set.iterator().next();
            return holder.fetch(message.catnip()).thenApply(data -> {
                String content = message.content().trim();
                if(content.startsWith(data.userMention)) return data.userMention;
                if(content.startsWith(data.memberMention)) return data.memberMention;
                return null;
            });
        };
    }

    @Nonnull
    @CheckReturnValue
    static PrefixProvider startingWith(@Nonnull String... options) {
        return (__, message) -> {
            String content = message.content().trim();
            for(String s : options) {
                if(content.startsWith(s)) return CompletableFuture.completedFuture(s);
            }
            return CompletableFuture.completedFuture(null);
        };
    }
}
