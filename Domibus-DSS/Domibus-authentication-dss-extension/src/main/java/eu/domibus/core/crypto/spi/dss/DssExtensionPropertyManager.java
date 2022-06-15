package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Property manager for the DSS extension.
 * Gives the opportunity to edit dss properties at runtime through the Domibus admin console.
 *
 * @author Thomas Dussart
 * @since 4.2
 */
public class DssExtensionPropertyManager extends DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    public static final String CUSTOM_TRUSTED_LISTS_PREFIX = "domibus.authentication.dss.custom.trusted.lists";
    public static final String CONSTRAINTS_PREFIX = "domibus.authentication.dss.constraints";

    public static final String AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL = "domibus.authentication.dss.current.official.journal.url";
    public static final String AUTHENTICATION_DSS_CURRENT_LOTL_URL = "domibus.authentication.dss.current.lotl.url";
    public static final String AUTHENTICATION_DSS_REFRESH_CRON = "domibus.authentication.dss.refresh.cron";
    public static final String AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA = "domibus.authentication.dss.exception.on.missing.revocation.data";
    public static final String AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS = "domibus.authentication.dss.check.revocation.for.untrusted.chains";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_HOST = "domibus.authentication.dss.proxy.https.host";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_PORT = "domibus.authentication.dss.proxy.https.port";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_USER = "domibus.authentication.dss.proxy.https.user";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD = "domibus.authentication.dss.proxy.https.password";
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS = "domibus.authentication.dss.proxy.https.excludedHosts";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_HOST = "domibus.authentication.dss.proxy.http.host";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PORT = "domibus.authentication.dss.proxy.http.port";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_USER = "domibus.authentication.dss.proxy.http.user";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD = "domibus.authentication.dss.proxy.http.password";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS = "domibus.authentication.dss.proxy.http.excludedHosts";
    public static final String DSS_PERFORM_CRL_CHECK = "domibus.dss.perform.crl.check";
    public static final String DSS_FULL_TLS_REFRESH = "domibus.authentication.dss.full.tls.refresh";
    public static final String DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH = "domibus.authentication.dss.custom.trusted.list.keystore.path";
    public static final String DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE = "domibus.authentication.dss.custom.trusted.list.keystore.type";
    public static final String DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD = "domibus.authentication.dss.custom.trusted.list.keystore.password";
    public static final String DSS_DATA_LOADER_SOCKET_TIMEOUT = "domibus.dss.data.loader.socket.timeout";
    public static final String DSS_DATA_LOADER_CONNECTION_TIMEOUT = "domibus.dss.data.loader.connection.timeout";
    public static final String AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE = "domibus.authentication.dss.password.encryption.active";
    public static final String AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_PROPERTIES = "domibus.authentication.dss.password.encryption.properties";
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_TYPE = "domibus.authentication.dss.official.journal.content.keystore.type";
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PATH = "domibus.authentication.dss.official.journal.content.keystore.path";
    public static final String AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PASSWORD = "domibus.authentication.dss.official.journal.content.keystore.password";
    public static final String AUTHENTICATION_DSS_LOTL_ROOT_SCHEME_INFO_URI = "domibus.authentication.dss.lotl.root.scheme.info.uri";
    public static final String AUTHENTICATION_DSS_CACHE_PATH = "domibus.authentication.dss.cache.path";
    public static final String AUTHENTICATION_DSS_CACHE_NAME = "domibus.authentication.dss.cache.name";
    public static final String EXCLUDE_PIVOT_FILE_REGEX = "domibus.exclude.pivot.file.regex";
    public static final String DSS_SSL_TRUST_STORE_PATH = "domibus.dss.ssl.trust.store.path";
    public static final String DSS_SSL_TRUST_STORE_PASSWORD = "domibus.dss.ssl.trust.store.password";
    public static final String DSS_SSL_TRUST_STORE_TYPE = "domibus.dss.ssl.trust.store.type";
    public static final String DSS_SSL_CACERT_PATH = "domibus.dss.ssl.cacert.path";
    public static final String DSS_SSL_CACERT_TYPE = "domibus.dss.ssl.cacert.type";
    public static final String DSS_SSL_CACERT_PASSWORD = "domibus.dss.ssl.cacert.password";

    //Dynamic custom trusted list properties
    public static final String DSS_CUSTOM_TRUSTED_LIST_1_URL = CUSTOM_TRUSTED_LISTS_PREFIX + ".list1.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_1_CODE = CUSTOM_TRUSTED_LISTS_PREFIX + ".list1.code";
    public static final String DSS_CUSTOM_TRUSTED_LIST_2_URL = CUSTOM_TRUSTED_LISTS_PREFIX + ".list2.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_2_CODE = CUSTOM_TRUSTED_LISTS_PREFIX + ".list2.code";
    public static final String DSS_CUSTOM_TRUSTED_LIST_3_URL = CUSTOM_TRUSTED_LISTS_PREFIX + ".list3.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_3_CODE = CUSTOM_TRUSTED_LISTS_PREFIX + ".list3.code";
    public static final String DSS_CUSTOM_TRUSTED_LIST_1 = CUSTOM_TRUSTED_LISTS_PREFIX + ".list1";
    public static final String DSS_CUSTOM_TRUSTED_LIST_2 = CUSTOM_TRUSTED_LISTS_PREFIX + ".list2";
    public static final String DSS_CUSTOM_TRUSTED_LIST_3 = CUSTOM_TRUSTED_LISTS_PREFIX + ".list3";
    public static final String DSS_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT = "domibus.authentication.dss.custom.trusted.list.for.multitenant";

    //Dynamic constraints properties
    public static final String DSS_CONSTRAINTS_CONSTRAINT1 = CONSTRAINTS_PREFIX + ".constraint1";
    public static final String DSS_CONSTRAINTS_CONSTRAINT1_NAME = CONSTRAINTS_PREFIX + ".constraint1.name";
    public static final String DSS_CONSTRAINTS_CONSTRAINT2 = CONSTRAINTS_PREFIX + ".constraint2";
    public static final String DSS_CONSTRAINTS_CONSTRAINT2_NAME = CONSTRAINTS_PREFIX + ".constraint2.name";
    public static final String DSS_CONSTRAINTS_CONSTRAINT1_STATUS = CONSTRAINTS_PREFIX + ".constraint1.status";
    public static final String DSS_CONSTRAINTS_CONSTRAINT2_STATUS = CONSTRAINTS_PREFIX + ".constraint2.status";
    public static final String DSS_EXTENSION_PROPERTIES = "authentication-dss-extension.properties";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public DssExtensionPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_LOTL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_FULL_TLS_REFRESH, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_REFRESH_CRON, DomibusPropertyMetadataDTO.Type.CRON, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_HOST, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PORT, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_USER, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.COMMA_SEPARATED_LIST, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_HOST, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PORT, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_USER, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.COMMA_SEPARATED_LIST, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_PERFORM_CRL_CHECK, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CUSTOM_TRUSTED_LISTS_PREFIX, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(CONSTRAINTS_PREFIX, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_1_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_1_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_2_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_2_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_3_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_3_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT1_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT2_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT1_STATUS, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT2_STATUS, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_PROPERTIES, DomibusPropertyMetadataDTO.Type.COMMA_SEPARATED_LIST, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_DATA_LOADER_SOCKET_TIMEOUT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_DATA_LOADER_CONNECTION_TIMEOUT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_OFFICIAL_JOURNAL_CONTENT_KEYSTORE_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_LOTL_ROOT_SCHEME_INFO_URI, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CACHE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CACHE_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT1, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT2, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_FOR_MULTITENANT, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(EXCLUDE_PIVOT_FILE_REGEX, DomibusPropertyMetadataDTO.Type.REGEXP, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_TRUST_STORE_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_TRUST_STORE_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_TRUST_STORE_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_CACERT_PATH, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_CACERT_TYPE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_SSL_CACERT_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_1, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_2, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_3, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL)

        );
        knownProperties = allProperties.stream().collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, domibusPropertyMetadataDTO -> domibusPropertyMetadataDTO));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

    @Override
    protected String getModulePropertiesHome() {
        return EXTENSIONS_CONFIG_HOME;
    }

    @Override
    protected String getPropertiesFileName() {
        return DSS_EXTENSION_PROPERTIES;
    }

    @Override
    public Optional<String> getConfigurationFileName(DomainDTO domain) {
        // intentionally return null as there is no property file for a domain
        return Optional.empty();
    }

}
