package eu.domibus.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

/**
 * <p>Configuration providing registration for the Bouncy Castle security provider and verification for the use of the
 * Unlimited Strength Jurisdiction.</p>
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1.4
 */
@Configuration
public class DomibusSecurityPolicyConfig {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSecurityPolicyConfig.class);

    // As recommended, make sure that the Sun security provider remains at a higher preference (i.e. index 2 on Weblogic)
    private static final int HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES = 3;

    @PostConstruct
    public void init() {
        LOG.info("Registering the Bouncy Castle provider as the third highest preferred security provider");
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES);
            LOG.debug("Security providers in order of preferences [{}]", Arrays.toString(Security.getProviders()));
        } catch (SecurityException e) {
            LOG.error("An error registering Bouncy Castle provider as the second highest preferred security provider", e);
        }

        checkUnlimitedStrengthJurisdiction();
    }

    private void checkUnlimitedStrengthJurisdiction() {
        int maxKeyLen = 0;
        try {
            maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        } catch (NoSuchAlgorithmException e) { /*ignore*/ }

        LOG.info("Using {} strength jurisdiction policy: maxKeyLen=[{}]",
                maxKeyLen == 128
                        ? "Limited"
                        : maxKeyLen == 2147483647
                            ? "Unlimited"
                            : "Unknown",
                maxKeyLen);
    }
}
