package eu.domibus.api.security.functions;

/**
 * Function interface ApplicationAuthenticatedProcedure is used with
 * AuthUtils for setting spring security context before invoking the method. After method is invoked security context is
 * cleared. For details see @AuthUtilsImpl.
 *
 * The interface is used for 'void' methods.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */

@FunctionalInterface
public interface ApplicationAuthenticatedProcedure {
    /**
     * Invoke function wrapped with spring security context.
     */
    public abstract void invoke();
}