package eu.domibus.core.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

/**
 * @author Sebastian Tincu
 * @author Cosmin Baciu
 * @since 4.2
 */
public class BouncyCastleInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BouncyCastleInitializer.class);

    // As recommended, make sure that the Sun security provider remains at a higher preference (i.e. index 2 on Weblogic)
    private static final int HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES = 3;

    private static final int LIMITED_STRENGTH_MAX_KEY_LENGTH = 128;

    private static final int UNLIMITED_STRENGTH_MAX_KEY_LENGTH = Integer.MAX_VALUE;

    public void registerBouncyCastle() {
        LOG.info("Registering the Bouncy Castle provider as the third highest preferred security provider");
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES);
            LOG.debug("Security providers in order of preferences [{}]", Arrays.toString(Security.getProviders()));
        } catch (SecurityException e) {
            LOG.error("An error registering Bouncy Castle provider as the second highest preferred security provider", e);
        }
    }

    public void checkStrengthJurisdictionPolicyLevel() {
        int maxKeyLen = 0;
        try {
            maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        } catch (NoSuchAlgorithmException e) { /*ignore*/ }

        LOG.info("Using {} strength jurisdiction policy: maxKeyLen=[{}]",
                maxKeyLen == LIMITED_STRENGTH_MAX_KEY_LENGTH
                        ? "Limited"
                        : maxKeyLen == UNLIMITED_STRENGTH_MAX_KEY_LENGTH
                        ? "Unlimited"
                        : "Unknown",
                maxKeyLen);
    }

}
