package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Load multiple OtherTrustedList objects based on Domibus nested property mechanism.
 * @see eu.europa.esig.dss.tsl.OtherTrustedList
 */
@Component
public class CustomTrustedListPropertyMapper extends PropertyGroupMapper<OtherTrustedList> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomTrustedListPropertyMapper.class);

    private static final String URL = "url";

    private static final String CODE = "code";

    public CustomTrustedListPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService) {
        super(domibusPropertyExtService);
    }

    public List<OtherTrustedList> map() {
        return super.map(
                CUSTOM_TRUSTED_LISTS_PREFIX
        );
    }

    @Override
    OtherTrustedList transform(Map<String, String> keyValues) {
        OtherTrustedList otherTrustedList = new OtherTrustedList();
        String customListKeystorePath = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH);
        String customListKeystoreType = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE);
        String customListKeystorePassword = domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD);
        String customListUrl = keyValues.get(URL);
        String customListCountryCode = keyValues.get(CODE);
        try {
            otherTrustedList.setTrustStore(initKeyStoreCertificateSource(customListKeystorePath,customListKeystoreType,customListKeystorePassword));
            otherTrustedList.setUrl(customListUrl);
            otherTrustedList.setCountryCode(customListCountryCode);
            LOG.debug("Custom trusted list with keystore path:[{}] and type:[{}], URL:[{}], customListCountryCode:[{}] will be added to DSS", customListKeystorePath, customListKeystoreType, customListUrl, customListCountryCode);
            return otherTrustedList;
        } catch (IOException e) {
            LOG.error("Error while configuring custom trusted list with keystore path:[{}],type:[{}] ", customListKeystorePath, customListKeystoreType, e);
            return null;
        }
    }

    protected KeyStoreCertificateSource initKeyStoreCertificateSource(String customListKeystorePath,String customListKeystoreType,String customListKeystorePassword) throws IOException {
        return new KeyStoreCertificateSource(new File(customListKeystorePath), customListKeystoreType, customListKeystorePassword);
    }
}
