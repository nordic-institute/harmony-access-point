package eu.domibus.core.payload.persistence;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class PayloadPersistenceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadPersistenceHelper.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    public boolean isPayloadEncryptionActive(UserMessage userMessage) {
        //EDELIVERY-4749 - SplitAndJoin limitation
        final boolean isPayloadEncryptionActive = !userMessage.isSplitAndJoin() && domibusConfigurationService.isPayloadEncryptionActive(domainContextProvider.getCurrentDomain());
        LOG.debug("Is payload encryption active? [{}]", isPayloadEncryptionActive);
        return isPayloadEncryptionActive;
    }

    /**
     * I will validate the payload (partInfo) size regardless the maxSize value defined in PMode - PayloadProfile
     *
     * @param legConfiguration
     * @param partInfoLength
     * @throws InvalidPayloadSizeException Exception thrown if payload size is greater than the maxSize defined in PMode
     */
    public void validatePayloadSize(@NotNull LegConfiguration legConfiguration, long partInfoLength) throws InvalidPayloadSizeException {
        validatePayloadSize(legConfiguration, partInfoLength, false);
    }

    /**
     * I will validate the payload (partInfo) size regardless the maxSize value defined in PMode - PayloadProfile
     *
     * @param legConfiguration
     * @param partInfoLength
     * @param isPayloadSavedAsync true is the payload was saved asynchronously, false by default
     * @throws InvalidPayloadSizeException Exception thrown if payload size is greater than the maxSize defined in PMode
     */
    public void validatePayloadSize(@NotNull LegConfiguration legConfiguration, long partInfoLength, boolean isPayloadSavedAsync) throws InvalidPayloadSizeException {
        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            LOG.debug("payload profile is not defined for leg [{}]", legConfiguration.getName());
            return;
        }
        final String payloadProfileName = profile.getName();
        final long payloadProfileMaxSize = legConfiguration.getPayloadProfile().getMaxSize();

        if (payloadProfileMaxSize < 0) {
            LOG.warn("No validation will be made for [{}] as maxSize has the value [{}]", payloadProfileName, payloadProfileMaxSize);
        }

        if (partInfoLength > payloadProfileMaxSize) {
            throw new InvalidPayloadSizeException("Payload size [" + partInfoLength + "] is greater than the maximum value " +
                    "defined [" + payloadProfileMaxSize + "] for profile [" + payloadProfileName + "]", isPayloadSavedAsync);
        }
    }
}
