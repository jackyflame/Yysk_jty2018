package im.socks.yysk.json;

/**
 * An unchecked exception to indicate that an input does not qualify as valid JSON.
 */
@SuppressWarnings("serial") // use default serial UID
public class ParseException extends RuntimeException {

    private final int offset;
    private final int line;
    private final int column;

    ParseException(String message, int offset, int line, int column) {
        super(message + " at " + line + ":" + column);
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the absolute index of the character at which the error occurred. The index of the first
     * character of a document is 0.
     *
     * @return the character offset at which the error occurred, will be &gt;= 0
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Returns the number of the line in which the error occurred. The first line counts as 1.
     *
     * @return the line in which the error occurred, will be &gt;= 1
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the index of the character at which the error occurred, relative to the line. The index
     * of the first character of a line is 0.
     *
     * @return the column in which the error occurred, will be &gt;= 0
     */
    public int getColumn() {
        return column;
    }

}
