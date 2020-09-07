package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DssExtensionPropertyManager extends DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssExtensionPropertyManager.class);
    
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_TYPE="domibus.authentication.dss.official.journal.content.keystore.type";
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PATH="domibus.authentication.dss.official.journal.content.keystore.path";
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PASSWORD="domibus.authentication.dss.official.journal.content.keystore.password";
    public static final String AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL="domibus.authentication.dss.current.official.journal.url";
    public static final String AUTHENTICATION_DSS_CURRENT_LOTL_URL="domibus.authentication.dss.current.lotl.url";
    public static final String AUTHENTICATION_DSS_LOTL_COUNTRY_CODE="domibus.authentication.dss.lotl.country.code";
    public static final String AUTHENTICATION_DSS_LOTL_ROOT_SCHEME_INFO_URI="domibus.authentication.dss.lotl.root.scheme.info.uri";
    public static final String AUTHENTICATION_DSS_CACHE_PATH="domibus.authentication.dss.cache.path";
    public static final String AUTHENTICATION_DSS_REFRESH_CRON="domibus.authentication.dss.refresh.cron";
    public static final String AUTHENTICATION_DSS_CONSTRAINT_NAME_0="domibus.authentication.dss.constraint.name[0]";
    public static final String AUTHENTICATION_DSS_CONSTRAINT_STATUS_0="domibus.authentication.dss.constraint.status[0]";
    public static final String AUTHENTICATION_DSS_CONSTRAINT_NAME_1="domibus.authentication.dss.constraint.name[1]";
    public static final String AUTHENTICATION_DSS_CONSTRAINT_STATUS_1="domibus.authentication.dss.constraint.status[1]";
    public static final String AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT="domibus.authentication.dss.enable.custom.trusted.list.for.multitenant";
    public static final String AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA="domibus.authentication.dss.exception.on.missing.revocation.data";
    public static final String AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS="domibus.authentication.dss.check.revocation.for.untrusted.chains";
    public static final String AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_URL_0="domibus.authentication.dss.custom.trusted.list.url[0]";
    public static final String AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_0="domibus.authentication.dss.custom.trusted.list.keystore.path[0]";
    public static final String AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_0="domibus.authentication.dss.custom.trusted.list.keystore.type[0]";
    public static final String AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_0="domibus.authentication.dss.custom.trusted.list.keystore.password[0]";
    public static final String AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_COUNTRY_CODE_0="domibus.authentication.dss.custom.trusted.list.country.code[0]";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_HOST="domibus.authentication.dss.proxy.https.host";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_PORT="domibus.authentication.dss.proxy.https.port";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_USER="domibus.authentication.dss.proxy.https.user";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD="domibus.authentication.dss.proxy.https.password";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS="domibus.authentication.dss.proxy.https.excludedhosts";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_HOST="domibus.authentication.dss.proxy.http.host";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PORT="domibus.authentication.dss.proxy.http.port";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_USER="domibus.authentication.dss.proxy.http.user";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD="domibus.authentication.dss.proxy.http.password";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS="domibus.authentication.dss.proxy.http.excludedhosts";
    public static final String EXCLUDE_PIVOT_FILE_REGEX="domibus.exclude.pivot.file.regex";
    public static final String AUTHENTICATION_DSS_CACHE_NAME="domibus.authentication.dss.cache.name";
    public static final String DSS_PERFORM_CRL_CHECK="domibus.dss.perform.crl.check";


    public DssExtensionPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                /*new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PASSWORD, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_LOTL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_LOTL_COUNTRY_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_LOTL_ROOT_SCHEME_INFO_URI, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CACHE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_REFRESH_CRON, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CONSTRAINT_NAME_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CONSTRAINT_STATUS_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CONSTRAINT_NAME_1, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CONSTRAINT_STATUS_1, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_ENABLE_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_URL_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_COUNTRY_CODE_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),*/
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_URL_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CUSTOM_TRUSTED_LIST_COUNTRY_CODE_0, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_REFRESH_CRON, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_HOST, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PORT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_USER, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_HOST, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PORT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_USER, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                //new DomibusPropertyMetadataDTO(EXCLUDE_PIVOT_FILE_REGEX, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                //new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CACHE_NAME, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_PERFORM_CRL_CHECK, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return null;
    }
}
