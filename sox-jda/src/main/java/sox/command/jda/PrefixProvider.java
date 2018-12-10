package sox.command.jda;

import net.dv8tion.jda.core.entities.Message;
import sox.Sox;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

@FunctionalInterface
public interface PrefixProvider {
    @Nonnull
    CompletionStage<List<String>> getPrefixes(@Nonnull Sox sox, @Nonnull Message message);

    @Nonnull
    @CheckReturnValue
    static <T> PrefixProvider fromServiceBlocking(@Nonnull Class<T> serviceClass, @Nonnull BiFunction<T, Message, List<String>> function) {
        return fromService(serviceClass, (service, message) -> CompletableFuture.completedFuture(function.apply(service, message)));
    }

    @Nonnull
    @CheckReturnValue
    static <T> PrefixProvider fromService(@Nonnull Class<T> serviceClass, @Nonnull BiFunction<T, Message, CompletionStage<List<String>>> function) {
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
            String id = message.getJDA().getSelfUser().getId();
            return CompletableFuture.completedFuture(Arrays.asList("<@" + id + ">", "<@!" + id + ">"));
        };
    }

    @Nonnull
    @CheckReturnValue
    static PrefixProvider startingWith(@Nonnull String... options) {
        List<String> list = Arrays.asList(options);
        return (__1, __2) -> CompletableFuture.completedFuture(list);
    }
}
