package multiplayerquiz.common.protocol;

/**
 *
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 3/29/2014
 */
public final class ProtocolException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ProtocolException(String message) {
        super(message);
    }
    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
