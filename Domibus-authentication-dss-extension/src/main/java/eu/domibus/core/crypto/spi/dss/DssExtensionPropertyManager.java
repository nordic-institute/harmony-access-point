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
    public static final String AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS = "domibus.authentication.dss.proxy.https.excludedhosts";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_HOST = "domibus.authentication.dss.proxy.http.host";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PORT = "domibus.authentication.dss.proxy.http.port";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_USER = "domibus.authentication.dss.proxy.http.user";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD = "domibus.authentication.dss.proxy.http.password";
    public static final String AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS = "domibus.authentication.dss.proxy.http.excludedhosts";
    public static final String DSS_PERFORM_CRL_CHECK = "domibus.dss.perform.crl.check";
    public static final String DSS_FULL_TLS_REFRESH = "domibus.authentication.dss.full.tls.refresh";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public DssExtensionPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_LOTL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CURRENT_OFFICIAL_JOURNAL_URL, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_LOTL_COUNTRY_CODE, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_FULL_TLS_REFRESH, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_REFRESH_CRON, DomibusPropertyMetadataDTO.Type.CRON, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_HOST, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PORT, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_USER, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.COMMA_SEPARATED_LIST, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_HOST, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PORT, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_USER, DomibusPropertyMetadataDTO.Type.STRING, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD, DomibusPropertyMetadataDTO.Type.PASSWORD, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS, DomibusPropertyMetadataDTO.Type.COMMA_SEPARATED_LIST, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DSS_PERFORM_CRL_CHECK, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.DSS_EXTENSION, DomibusPropertyMetadataDTO.Usage.GLOBAL));

        knownProperties = allProperties.stream().collect(Collectors.toMap(DomibusPropertyMetadataDTO::getName, domibusPropertyMetadataDTO -> domibusPropertyMetadataDTO));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }
}
