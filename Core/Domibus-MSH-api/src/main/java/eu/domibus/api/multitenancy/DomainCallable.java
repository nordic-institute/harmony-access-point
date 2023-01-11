package eu.domibus.api.multitenancy;

import java.util.concurrent.Callable;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class DomainCallable<T> implements Callable<T> {

    protected DomainContextProvider domainContextProvider;
    protected Callable<T> callable;
    protected Domain domain;

    public DomainCallable(DomainContextProvider domainContextProvider, Callable<T> callable) {
        this.domainContextProvider = domainContextProvider;
        this.callable = callable;
        this.domain = null;
    }
    public DomainCallable(DomainContextProvider domainContextProvider, Callable<T> callable, Domain domain) {
        this.domainContextProvider = domainContextProvider;
        this.callable = callable;
        this.domain = domain;
    }

    @Override
    public T call() throws Exception {
        if(domain == null) {
            domainContextProvider.clearCurrentDomain();
        } else {
            domainContextProvider.setCurrentDomain(domain);
        }
        return callable.call();
    }
}
