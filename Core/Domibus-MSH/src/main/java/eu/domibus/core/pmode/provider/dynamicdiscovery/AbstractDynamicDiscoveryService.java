package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.logging.IDomibusLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.*;

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
    protected abstract IDomibusLogger getLogger();

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
    protected abstract String getDefaultDiscoveryPartyIdType();

    /**
     * Get responder role specific to implementation of the dynamic discovery service
     *
     * @return responder role
     */
    protected abstract String getDefaultResponderRole();

    /**
     * get allowed SMP certificate policy OIDs
     *
     * @return list of certificate policy OIDs
     */
    protected List<String> getAllowedSMPCertificatePolicyOIDs() {
        final String allowedCertificatePolicyId = getTrimmedDomibusProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_POLICY);
        if (StringUtils.isBlank(allowedCertificatePolicyId)) {
            getLogger().debug("The value for property [{}] is empty.", DynamicDiscoveryService.DYNAMIC_DISCOVERY_CERT_POLICY);
            return Collections.emptyList();
        } else {
            return Arrays.asList(allowedCertificatePolicyId.split("\\s*,\\s*"));
        }
    }

    public String getPartyIdType() {
        String propVal = getTrimmedDomibusProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_PARTYID_TYPE);
        // if is null - this means property is commented-out and default value must be set.
        // else if is empty - property is set in domibus.properties as empty string and the right value for the
        // ebMS 3.0  PartyId/@type is null value!
        if (propVal == null) {
            propVal = getDefaultDiscoveryPartyIdType();
        } else if (StringUtils.isEmpty(propVal)) {
            propVal = null;
        }
        return propVal;
    }

    public String getResponderRole() {
        String propVal = getTrimmedDomibusProperty(DynamicDiscoveryService.DYNAMIC_DISCOVERY_PARTYID_RESPONDER_ROLE);
        if (StringUtils.isEmpty(propVal)) {
            propVal = getDefaultResponderRole();
        }
        return propVal;
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
