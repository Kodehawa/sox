package sox.command;

@FunctionalInterface
public interface UnmatchedCommandHandler<M> {
    void handleUnmatchedCommand(M message);
}
