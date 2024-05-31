package eu.domibus.api.multitenancy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;

/**
 * Wrapper for the Runnable class to be executed. Clear first the domain set on the thread before execution.
 *
 * @author Cosmin Baciu
 * @since 4.0.1
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
        SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
        return runnable.call();
    }
}
