package eu.domibus.core.crypto;

import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Component("TLSCryptoService") //todo: try to avoid qualifying
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TLSDomainCryptoServiceSpiImpl extends BaseDomainCryptoServiceSpiImpl implements DomainCryptoServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSDomainCryptoServiceSpiImpl.class);

    @Autowired
    private TLSReaderService tlsReader;

    private TLSClientParametersType params;

    @Override
    public void init() {
        params = tlsReader.getTlsClientParametersType(domain.getCode());
        super.init();
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        //todo: decide if from domibus property or from clientauth.xml file??
        return "test123";
    }

    @Override
    protected String getPrivateKeyAlias() {
        //todo: decide if //from domibus property or from clientauth.xml file??
        return "certificate";
    }

    @Override
    protected String getKeystoreLocation() {
        return params.getKeyManagers().getKeyStore().getFile();
    }

    @Override
    protected String getKeystorePassword() {
        return params.getKeyManagers().getKeyStore().getPassword();
    }

    @Override
    protected String getKeystoreType() {
        return params.getKeyManagers().getKeyStore().getType();
    }

    @Override
    protected String getTrustStoreLocation() {
        return params.getTrustManagers().getKeyStore().getFile();
    }

    @Override
    protected String getTrustStorePassword() {
        return params.getTrustManagers().getKeyStore().getPassword();
    }

    @Override
    public String getTrustStoreType() {
        return params.getTrustManagers().getKeyStore().getType();
    }

    @Override
    public String getIdentifier() {
        return "TLSCryptoService";
    }
}
