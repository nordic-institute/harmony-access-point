package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.logging.DomibusLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Abstract class implement common methods for Peppol and Oasis dynamic discovery
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
public abstract class AbstractDynamicDiscoveryService {

    /**
     * Return implementations class logger
     *
     * @return Domibus logger.
     */
    protected abstract DomibusLogger getLogger();

    /**
     * Return trimmed domibus property value
     *
     * @param propertyName
     * @return value for given domibus property name
     */
    protected abstract String getTrimmedDomibusProperty(String propertyName);

    /**
     * Get Default Discovery partyId type specific to implementation of the dynamic discovery service
     *
     * @return discovery party type
     */
    protected abstract String getPartyIdTypePropertyName();

    /**
     * Get responder role specific to implementation of the dynamic discovery service
     *
     * @return responder role
     */
    protected abstract String getPartyIdResponderRolePropertyName();

    /**
     * get allowed SMP certificate policy OIDs
     *
     * @return list of certificate policy OIDs
     */
    protected List<String> getAllowedSMPCertificatePolicyOIDs() {
        final String allowedCertificatePolicyId = getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);
        if (StringUtils.isBlank(allowedCertificatePolicyId)) {
            getLogger().debug("The value for property [{}] is empty.", DOMIBUS_DYNAMICDISCOVERY_CLIENT_CERTIFICATE_POLICY_OID_VALIDATION);
            return Collections.emptyList();
        } else {
            return Arrays.asList(allowedCertificatePolicyId.split("\\s*,\\s*"));
        }
    }

    public String getPartyIdType() {
        String propertyName = getPartyIdTypePropertyName();
        // if is null - this means property is commented-out and default value must be set.
        // else if is empty - property is set in domibus.properties as empty string and the right value for the
        // ebMS 3.0  PartyId/@type is null value!
        return StringUtils.trimToNull(getTrimmedDomibusProperty(propertyName));
    }

    public String getResponderRole() {
        String propertyName = getPartyIdResponderRolePropertyName();
        return getTrimmedDomibusProperty(propertyName);
    }

    /**
     * Method validates serviceActivationDate and serviceExpirationDate dates.
     * A missing/null activation date is interpreted as “valid".
     * A missing/null expiration date is interpreted as “valid until eternity”.
     *
     * @param serviceActivationDate activate date from element Endpoint/ServiceActivationDate
     * @param serviceExpirationDate expiration date from element Endpoint/ServiceExpirationDate
     * @return true if the endpoint is valid for the current date. Else return false.
     */
    public boolean isValidEndpoint(Date serviceActivationDate, Date serviceExpirationDate) {
        Date currentDate = Calendar.getInstance().getTime();
        if (serviceActivationDate != null && currentDate.before(serviceActivationDate)) {
            getLogger().warn("Found endpoint which is not yet activated! Endpoint's activation date: [{}]!", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(serviceActivationDate));
            return false;
        }

        if (serviceExpirationDate != null && currentDate.after(serviceExpirationDate)) {
            getLogger().warn("Found endpoint, which is expired! Endpoint's expiration date: [{}]!", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(serviceExpirationDate));
            return false;
        }
        return true;
    }
}
