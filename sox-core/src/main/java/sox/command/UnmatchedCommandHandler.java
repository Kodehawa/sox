package sox.command;

import sox.Sox;

@FunctionalInterface
public interface UnmatchedCommandHandler<M> {
    void handleUnmatchedCommand(Sox sox, M message, String parsedName, String remainingInput);
}
