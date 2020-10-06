package eu.domibus.api.security.functions;


/**
 * Function interface ApplicationAuthenticatedProcedure is used with
 * AuthUtils for setting spring security context before invoking the method. After method is invoked security context is
 * cleared. For details see @AuthUtilsImpl.
 *
 * The interface is used when method returns value.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
@FunctionalInterface
public interface ApplicationAuthenticatedFunction <R> {
    /**
     * Invoke function wrapped with spring security context and return value
     * @return value
     */
    public abstract R invoke();
}