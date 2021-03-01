package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.source.TLSource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * @author Thomas Dussart
 * @see TLSource
 * @since 4.1
 * <p>
 * Load multiple OtherTrustedList objects based on Domibus nested property mechanism.
 */
@Component
public class CustomTrustedListPropertyMapper extends PropertyGroupMapper<TLSource> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomTrustedListPropertyMapper.class);

    private static final String URL = "url";

    public CustomTrustedListPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService) {
        super(domibusPropertyExtService);
    }

    public List<TLSource> map() {
        return super.map(
                CUSTOM_TRUSTED_LISTS_PREFIX
        );
    }

    @Override
    TLSource transform(Map<String, String> keyValues) {
        TLSource otherTrustedList = new TLSource();
        String customListKeystorePath = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH);
        String customListKeystoreType = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE);
        String customListKeystorePassword = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD);
        String customListUrl = keyValues.get(URL);
        try {
            otherTrustedList.setUrl(customListUrl);
            otherTrustedList.setCertificateSource(new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword));
            LOG.debug("Custom trusted list with keystore path:[{}] and type:[{}], URL:[{}]will be added to DSS", customListKeystorePath, customListKeystoreType, customListUrl);
            return otherTrustedList;
        } catch (IOException e) {
            LOG.error("Error while configuring custom trusted list with keystore path:[{}],type:[{}] ", customListKeystorePath, customListKeystoreType, e);
            return null;
        }
    }

    protected KeyStoreCertificateSource initKeyStoreCertificateSource(String customListKeystorePath, String customListKeystoreType, String customListKeystorePassword) throws IOException {
        return new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword);
    }
}
