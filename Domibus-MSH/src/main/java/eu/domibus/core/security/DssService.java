package eu.domibus.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class DssService extends java.security.Provider.Service {

    private boolean registered;

    private DomibusProvider provider;

    @Autowired
    public DssService(DomibusProvider provider) {
        super(provider, "CertPathValidator", "PKIX", "org.bouncycastle.jce.provider.PKIXCertPathValidatorSpi", null, null);
        this.provider = provider;
    }

    @Override
    public Object newInstance(Object constructorParameter)
            throws NoSuchAlgorithmException {
        return new CertPathValidatorSpi() {

            @Override
            public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
                return () -> Boolean.TRUE;
            }
        };
    }


}
