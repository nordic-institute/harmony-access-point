package eu.domibus.api.multitenancy;

/**
 * Wrapper for the Runnable class to be executed. Clear first the domain set on the thread before execution.
 *
 * @author Cosmin Baciu
 * @since 4.0.1
 */
public class ClearDomainRunnable implements Runnable {

    protected DomainContextProvider domainContextProvider;
    protected Runnable runnable;

    public ClearDomainRunnable(final DomainContextProvider domainContextProvider, final Runnable runnable) {
        this.domainContextProvider = domainContextProvider;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        domainContextProvider.clearCurrentDomain();
        runnable.run();
    }
}
