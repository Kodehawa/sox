package sox.command.argument;

/**
 * Provides a similar but superior API over {@link Arguments#mark() mark}/{@link Arguments#reset() reset},
 * supporting nesting of mark/reset blocks without one interfering with the other.
 */
public class MarkedBlock {
    private final Arguments arguments;
    private int offset;

    public MarkedBlock(Arguments arguments) {
        this.arguments = arguments;
        mark();
    }

    /**
     * Updates the reset offset. After calling this method, any resets will return to the
     * current offset.
     */
    public void mark() {
        this.offset = arguments.getOffset();
    }

    /**
     * Resets to the currently marked offset.
     */
    public void reset() {
        arguments.setOffset(offset);
    }
}
