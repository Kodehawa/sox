package sox.command.catnip;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.user.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

class SelfInfoHolder {
    private State state;

    CompletionStage<State> fetch(Catnip catnip) {
        if(state != null) return CompletableFuture.completedFuture(state);
        User self = catnip.cache().selfUser();
        CompletionStage<User> fetchSelf;
        if(self != null) {
            fetchSelf = CompletableFuture.completedFuture(self);
        } else {
            fetchSelf = catnip.rest().user().getCurrentUser();
        }
        return fetchSelf.thenApply(user -> {
            State state = new State(user.id());
            this.state = state;
            return state;
        });
    }

    static class State {
        final String id;
        final String userMention;
        final String memberMention;

        State(String id) {
            this.id = id;
            this.userMention = "<@" + id + ">";
            this.memberMention = "<@!" + id + ">";
        }
    }
}
