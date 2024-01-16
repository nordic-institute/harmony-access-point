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

    private static final int LIMITED_STRENGTH_MAX_KEY_LENGTH = 128;

    private static final int UNLIMITED_STRENGTH_MAX_KEY_LENGTH = Integer.MAX_VALUE;

    public void registerBouncyCastle(Integer pos) {
        LOG.info("Registering the Bouncy Castle provider at position {}", pos == null ? "'last'" : pos.toString());
        try {
            if (pos == null) {
                Security.addProvider(new BouncyCastleProvider());
            } else {
                Security.insertProviderAt(new BouncyCastleProvider(), pos);
            }
            LOG.debug("Security providers in order of preferences [{}]", Arrays.toString(Security.getProviders()));
        } catch (SecurityException e) {
            LOG.error("An error registering Bouncy Castle provider", e);
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
