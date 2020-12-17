
package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service("keystorePasswordCallback")
public class SimpleKeystorePasswordCallback implements CallbackHandler {

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;


    @Override
    public void handle(final Callback[] callbacks) {
        for (final Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                final WSPasswordCallback pc = (WSPasswordCallback) callback;
                final String privateKeyAlias = pc.getIdentifier();
                final Domain currentDomain = domainProvider.getCurrentDomain();
                final String privateKeyPassword = multiDomainCertificateProvider.getPrivateKeyPassword(currentDomain, privateKeyAlias);
                pc.setPassword(privateKeyPassword);
            }
        }
    }
}
