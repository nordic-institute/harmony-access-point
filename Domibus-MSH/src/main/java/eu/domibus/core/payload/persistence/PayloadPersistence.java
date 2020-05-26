package eu.domibus.core.payload.persistence;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface PayloadPersistence {

    int DEFAULT_BUFFER_SIZE = 32 * 1024;

    void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration) throws IOException;

    void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception;

    /**
     * I will validate the payload (partInfo) size regardless the maxSize value defined in PMode - PayloadProfile
     *
     * @param legConfiguration
     * @param partInfoLength
     * @throws InvalidPayloadSizeException Exception thrown if payload size is greather than the maxSize defined in PMode
     */
    default void validatePayloadSize(@NotNull LegConfiguration legConfiguration, long partInfoLength) throws InvalidPayloadSizeException {
        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            getLogger().debug("payload profile is not defined for leg [{}]", legConfiguration.getName());
            return;
        }
        final String payloadProfileName = profile.getName();
        final int payloadProfileMaxSize = legConfiguration.getPayloadProfile().getMaxSize();

        if (payloadProfileMaxSize < 0) {
            getLogger().warn("No validation will be made for [{}] as maxSize has the value [{}]", payloadProfileName, payloadProfileMaxSize);
        }

        if (partInfoLength > payloadProfileMaxSize) {
            throw new InvalidPayloadSizeException("Payload size [" + partInfoLength + "] is greater than the maximum value defined [" + payloadProfileMaxSize + "] for profile [" + payloadProfileName + "]");
        }
    }

    DomibusLogger getLogger();
}
