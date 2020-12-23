package eu.domibus.core.crypto;

import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import eu.domibus.core.exception.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.apache.wss4j.common.crypto.Merlin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TLSDomainCryptoServiceSpiImpl extends BaseDomainCryptoServiceSpiImpl implements DomainCryptoServiceSpi {

    @Autowired
    private TLSReaderService tlsReader;

    private TLSClientParametersType params;

    @Override
    public void init() {
        params = tlsReader.getTlsClientParametersType(domain.getCode());
        super.init();
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

    @Override
    protected Properties getKeystoreProperties() {
        // mark we don't care for them for now
        return new Properties();
    }

    @Override
    public String getPrivateKeyPassword(String alias) {
        // not used for now anyway
        return params.getKeyManagers().getKeyPassword();
    }

    @Override
    protected String getPrivateKeyAlias() {
        // not used for now anyway
        return params.getCertAlias();
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

}
