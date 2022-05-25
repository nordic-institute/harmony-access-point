package eu.domibus.plugin.ws.exception;

/**
 * WSPluginException
 *
 * @author François Gautier
 * @since 4.2
 */
public class WSPluginException extends RuntimeException {

    /**
     * Creates a new <code>WSPluginException/code>.
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public WSPluginException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>WSPluginException</code>.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public WSPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
