package eu.domibus.api.multitenancy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;

/**
 * Wrapper for the Callable class to be executed, preserving the security context
 *
 * @author Ion perpegel
 * @since 5.1.5
 */
public class SetAuthRunnable<T> implements Callable<T> {
    protected Callable<T> runnable;

    final Authentication currentAuthentication;

    public SetAuthRunnable(final Authentication currentAuthentication, final Callable<T> runnable) {
        this.runnable = runnable;
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public T call() throws Exception {
        try {
            SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
            return runnable.call();
        }
        finally { // TODO IB make sure it's not breaking things
            SecurityContextHolder.clearContext();
        }
    }
}
