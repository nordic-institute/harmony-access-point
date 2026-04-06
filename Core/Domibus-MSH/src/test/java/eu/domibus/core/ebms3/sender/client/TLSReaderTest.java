package eu.domibus.core.ebms3.sender.client;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.property.DomibusConfigurationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class TLSReaderTest {

    public static final String CONFIG_LOCATION = "configLocation";

    @Injectable
    private Path domainSpecificPath;

    @Injectable
    private Path defaultPath;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomibusLocalCacheService domibusLocalCacheService;

    @Injectable
    Environment environment;

    @Tested
    private TLSReaderServiceImpl tlsReader;

    private String domainCode;

    private Optional<Path> clientAuthenticationPath;

    boolean domainSpecificPathExists, defaultPathExists;

    @Before
    public void setUp() {
        givenPathsMocks();
    }

    @Test
    public void returnsTheClientAuthenticationFromTheDomainSpecificPathIfPresent() {
        givenConfigLocation();
        givenDomainCode("TAXUD");
        givenDomainSpecificPathFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertSame("Should have returned the domain specific path if present", clientAuthenticationPath.get(), domainSpecificPath);
    }

    @Test
    public void returnsTheClientAuthenticationFromTheDefaultPathIfPresentWhenTheDomainSpecificPathDoesNotExist() {
        givenConfigLocation();
        givenDomainCode("TAXUD");
        givenDomainSpecificPathNotFound();
        givenDefaultPathFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertSame("Should have returned the default path if present when the domain specific path is missing", clientAuthenticationPath.get(), defaultPath);
    }

    @Test
    public void returnsNoClientAuthenticationWhenTheDefaultPathAndTheDomainSpecificPathDoNotExist() {
        givenConfigLocation();
        givenDomainCode("TAXUD");
        givenDomainSpecificPathNotFound();
        givenDefaultPathNotFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertFalse("Should have returned no path when the domain specific and the default paths are both missing", clientAuthenticationPath.isPresent());
    }

    @Test
    public void stripsTheDomainCodeForWhitespacesBeforeLookingUpTheClientAuthenticationFromTheDomainSpecificPath() {
        givenConfigLocation();
        givenDomainCode("   TAXUD\t ");
        givenDomainSpecificPathFound();
        new MockUp<Paths>() {
            @Mock
            public Path get(String first, String... more) {
                if(!isDomainSpecificScenario(more)) {
                    throw new IllegalArgumentException("The domain code should have been stripped down of whitespace characters");
                }
                return domainSpecificPath;
            }
        };

        whenRetrievingTheClientAuthenticationPath();
    }

    @Test
    public void resolvePlaceholders_replacesSinglePlaceholder() {
        givenProperty("SECURITY_KEYSTORE_PASSWORD", "secret");
        String config = "<keyStore password=\"${SECURITY_KEYSTORE_PASSWORD}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<keyStore password=\"secret\"/>", result);
    }

    @Test
    public void resolvePlaceholders_replacesMultiplePlaceholders() {
        givenProperty("DB_USER", "dbu");
        givenProperty("DB_PASSWORD", "dbp");
        String config = "<root a=\"${DB_USER}\" b=\"${DB_PASSWORD}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root a=\"dbu\" b=\"dbp\"/>", result);
    }

    @Test
    public void resolvePlaceholders_failsWhenPlaceholderIsMissing() {
        String config = "<keyStore password=\"${TLS_KEYSTORE_PASSWORD}\"/>";

        try {
            tlsReader.resolvePlaceholders(config, null);
            Assert.fail("Expected IllegalStateException when placeholder is missing");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("TLS_KEYSTORE_PASSWORD"));
            Assert.assertTrue(ex.getMessage().contains("clientauthentication.xml"));
        }
    }

    @Test
    public void resolvePlaceholders_usesDefaultWhenPropertyIsMissing() {
        String config = "<root secureSocketProtocol=\"${TLS_SOCKET_PROTOCOL:TLSv1.2}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root secureSocketProtocol=\"TLSv1.2\"/>", result);
    }

    @Test
    public void resolvePlaceholders_propertyOverridesDefault() {
        givenProperty("TLS_SOCKET_PROTOCOL", "TLSv1.3");
        String config = "<root secureSocketProtocol=\"${TLS_SOCKET_PROTOCOL:TLSv1.2}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root secureSocketProtocol=\"TLSv1.3\"/>", result);
    }

    @Test
    public void resolvePlaceholders_emptyDefaultIsValid() {
        String config = "<root zone=\"${SML_ZONE:}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root zone=\"\"/>", result);
    }

    @Test
    public void resolvePlaceholders_mixedDefaultAndRequired() {
        String config = "<root type=\"${TLS_STORE_TYPE:PKCS12}\" password=\"${TLS_KEYSTORE_PASSWORD}\"/>";

        try {
            tlsReader.resolvePlaceholders(config, null);
            Assert.fail("Expected IllegalStateException for missing required placeholder");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("TLS_KEYSTORE_PASSWORD"));
        }
    }

    @Test
    public void resolvePlaceholders_defaultWithXmlSpecialChars() {
        String config = "<root value=\"${MY_VAR:a&b}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root value=\"a&amp;b\"/>", result);
    }

    @Test
    public void resolvePlaceholders_escapesXmlSpecialCharacters() {
        givenProperty("SECURITY_KEYSTORE_PASSWORD", "pa&<\\\">'");
        String config = "<keyStore password=\"${SECURITY_KEYSTORE_PASSWORD}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<keyStore password=\"pa&amp;&lt;\\&quot;&gt;&apos;\"/>", result);
    }

    @Test
    public void resolvePlaceholders_passwordContainingPlaceholderSyntax() {
        givenProperty("TLS_KEYSTORE_PASSWORD", "pa${foo}ss");
        String config = "<keyStore password=\"${TLS_KEYSTORE_PASSWORD}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<keyStore password=\"pa${foo}ss\"/>", result);
    }

    @Test
    public void resolvePlaceholders_unclosedPlaceholderThrows() {
        String config = "<root value=\"${UNCLOSED\"/>";

        try {
            tlsReader.resolvePlaceholders(config, null);
            Assert.fail("Expected IllegalStateException for unclosed placeholder");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("Unclosed placeholder"));
        }
    }

    @Test
    public void resolvePlaceholders_resolvesDotNotationProperties() {
        givenProperty("domibus.config.location", "/etc/harmony-ap");
        String config = "<root path=\"${domibus.config.location}/certs\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<root path=\"/etc/harmony-ap/certs\"/>", result);
    }

    @Test
    public void resolvePlaceholders_mixesDotNotationAndEnvVars() {
        givenProperty("domibus.config.location", "/etc/harmony-ap");
        givenProperty("TLS_KEYSTORE_PASSWORD", "secret");
        String config = "<keyStore file=\"${domibus.config.location}/tls.p12\" password=\"${TLS_KEYSTORE_PASSWORD}\"/>";

        String result = tlsReader.resolvePlaceholders(config, null);

        Assert.assertEquals("<keyStore file=\"/etc/harmony-ap/tls.p12\" password=\"secret\"/>", result);
    }

    private void givenConfigLocation() {
        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = CONFIG_LOCATION;
        }};
    }

    private void givenProperty(String name, String value) {
        new Expectations() {{
            environment.getProperty(name);
            result = value;
        }};
    }

    private void givenDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    private void givenDomainSpecificPathFound() {
        domainSpecificPathExists = true;
    }

    private void givenDomainSpecificPathNotFound() {
        domainSpecificPathExists = false;
    }

    private void givenDefaultPathFound() {
        defaultPathExists = true;
    }

    private void givenDefaultPathNotFound() {
        defaultPathExists = false;
    }

    // There is no nicer way to mock static methods even in JMockit and new MockUp definitions overwrite previous ones so they need to be defined once per mocked up class
    private void givenPathsMocks() {
        new MockUp<Paths>() {
            @Mock
            public Path get(String first, String... more) {
                return isDomainSpecificScenario(more) ? domainSpecificPath : defaultPath;
            }
        };

        new MockUp<Files>() {
            @Mock
            public boolean exists(Path path, LinkOption... options) {
                if(path == domainSpecificPath) {
                    return domainSpecificPathExists;
                }else if(path == defaultPath) {
                    return defaultPathExists;
                }else {
                    throw new IllegalArgumentException("Should have been invoked with the domain specific path or the default path");
                }
            }
        };
    }

    private boolean isDomainSpecificScenario(String... more) {
        return Arrays.stream(more).anyMatch(el-> el.startsWith(StringUtils.stripToEmpty(domainCode)));
    }

    private void whenRetrievingTheClientAuthenticationPath() {
        clientAuthenticationPath = tlsReader.getClientAuthenticationPath(domainCode);
    }
}
