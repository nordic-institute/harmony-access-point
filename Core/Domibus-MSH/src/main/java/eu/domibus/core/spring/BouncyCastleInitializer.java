package eu.domibus.core.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.env.PropertySource;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_PROVIDER_BOUNCY_CASTLE_POSITION;

/**
 * @author Sebastian Tincu
 * @author Cosmin Baciu
 * @since 4.2
 */
public class BouncyCastleInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BouncyCastleInitializer.class);

    // As recommended, make sure that the Sun security provider remains at a higher preference (i.e. index 2 on Weblogic)
    private static final int HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES = 3;

    private final int bouncyCastlePosition;

    private static final int LIMITED_STRENGTH_MAX_KEY_LENGTH = 128;

    private static final int UNLIMITED_STRENGTH_MAX_KEY_LENGTH = Integer.MAX_VALUE;

    public BouncyCastleInitializer(PropertySource propertySource) {
        bouncyCastlePosition = getPropertyValue(propertySource);
    }

    public void registerBouncyCastle() {
        LOG.info("Registering the Bouncy Castle provider at position [{}] in the list of security providers.", bouncyCastlePosition);
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), bouncyCastlePosition);
            LOG.info("Security providers in order of preferences [{}]", Arrays.toString(Security.getProviders()));
        } catch (SecurityException e) {
            LOG.error("An error registering Bouncy Castle provider at position [{}] in the list of security providers",
                    bouncyCastlePosition, e);
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

    private int getPropertyValue(PropertySource propertySource) {
        String propertyValue = (String) propertySource.getProperty(DOMIBUS_SECURITY_PROVIDER_BOUNCY_CASTLE_POSITION);
        if (StringUtils.isBlank(propertyValue)) {
            LOG.warn("Empty value for property [{}]. Using the default value [{}].",
                    DOMIBUS_SECURITY_PROVIDER_BOUNCY_CASTLE_POSITION, HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES);
            return HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES;
        }
        try {
            LOG.debug("Read value [{}] for property [{}].", propertyValue, DOMIBUS_SECURITY_PROVIDER_BOUNCY_CASTLE_POSITION);
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException ex) {
            LOG.warn("Wrong integer format for property [{}]. Using the default value [{}].",
                    DOMIBUS_SECURITY_PROVIDER_BOUNCY_CASTLE_POSITION, HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES);
            return HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES;
        }
    }
}