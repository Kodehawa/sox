package sox.command.argument;

public class MarkedBlock implements AutoCloseable {
    private final Arguments arguments;
    private boolean shouldReset = false;
    private int offset;

    public MarkedBlock(Arguments arguments) {
        this.arguments = arguments;
        mark();
    }

    public void mark() {
        this.offset = arguments.getOffset();
    }

    public void reset() {
        shouldReset = true;
    }

    public void reset(boolean shouldReset) {
        this.shouldReset = shouldReset;
    }

    @Override
    public void close() {
        if(shouldReset) {
            arguments.setOffset(offset);
        }
    }
}
