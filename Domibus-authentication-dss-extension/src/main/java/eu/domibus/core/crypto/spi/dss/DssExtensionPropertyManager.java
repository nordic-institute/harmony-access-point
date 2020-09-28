package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class DssExtensionPropertyManager extends DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    public static final String CUSTOM_TRUSTED_LIST_URL_PROPERTY = "domibus.authentication.dss.custom.trusted.lists";
    public static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME = "domibus.authentication.dss.constraints";

    public static final String AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL="domibus.authentication.dss.current.official.journal.url";
    public static final String AUTHENTICATION_DSS_CURRENT_LOTL_URL="domibus.authentication.dss.current.lotl.url";
    public static final String AUTHENTICATION_DSS_LOTL_COUNTRY_CODE="domibus.authentication.dss.lotl.country.code";
    public static final String AUTHENTICATION_DSS_REFRESH_CRON="domibus.authentication.dss.refresh.cron";
    public static final String AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA="domibus.authentication.dss.exception.on.missing.revocation.data";
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
    public static final String DSS_CUSTOM_TRUSTED_LIST_1_URL = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list1.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_1_CODE = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list1.code";
    public static final String DSS_CUSTOM_TRUSTED_LIST_2_URL = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list2.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_2_CODE = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list2.code";
    public static final String DSS_CUSTOM_TRUSTED_LIST_3_URL = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list3.url";
    public static final String DSS_CUSTOM_TRUSTED_LIST_3_CODE = CUSTOM_TRUSTED_LIST_URL_PROPERTY+".list3.code";

    public static final String DSS_CONSTRAINTS_CONSTRAINT1_NAME=DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME+".constraint1.name";
    public static final String DSS_CONSTRAINTS_CONSTRAINT2_NAME=DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME+".constraint2.name";
    public static final String DSS_CONSTRAINTS_CONSTRAINT1_STATUS=DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME+".constraint1.status";
    public static final String DSS_CONSTRAINTS_CONSTRAINT2_STATUS=DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME+".constraint2.status";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public DssExtensionPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_LOTL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_LOTL_COUNTRY_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
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
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_1_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_1_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_2_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_2_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_3_URL, DomibusPropertyMetadataDTO.Type.URI, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CUSTOM_TRUSTED_LIST_3_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT1_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT2_NAME, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
        new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT1_STATUS, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_CONSTRAINTS_CONSTRAINT2_STATUS, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS, DomibusPropertyMetadataDTO.Usage.GLOBAL)
        );
        knownProperties = allProperties.stream().collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, domibusPropertyMetadataDTO -> domibusPropertyMetadataDTO));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }
}
