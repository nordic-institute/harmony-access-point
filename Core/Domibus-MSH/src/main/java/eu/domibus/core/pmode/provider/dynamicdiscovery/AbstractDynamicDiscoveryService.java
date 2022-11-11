package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;

import java.util.*;
import java.util.stream.Collectors;

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
     * Retrieves the map containing the mappings between Security Profiles and Transport Profiles
     *
     * @return map of Security Profiles and Transport Profiles
     */
    protected abstract Map<SecurityProfile, String> getSecurityProfileTransportProfileMap();

    /**
     * Returns a list of Security Profile names in the priority order specified in the properties file
     *
     * @return a list of ordered Security Profile names
     */
    protected List<SecurityProfile> getSecurityProfilesPriorityList() {
        String priorityString = getTrimmedDomibusProperty(DOMIBUS_SECURITY_PROFILE_ORDER);
        if (priorityString == null) {
            getLogger().warn("The property {} was not specified in the properties file", DOMIBUS_SECURITY_PROFILE_ORDER);
            return null;
        }
        return Arrays.stream(priorityString.split(","))
                .map(p -> {
                    try {
                        return SecurityProfile.valueOf(p.trim());
                    } catch (IllegalArgumentException e) {
                        getLogger().warn("Invalid Security profile specified in the property [{}]", DOMIBUS_SECURITY_PROFILE_ORDER);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of Transport Profiles that are extracted from the Processes list
     *
     * @return a list of Transport profiles
     */
    protected List<String> retrieveTransportProfilesFromProcesses(List<ProcessType> processes) {
        List<String> transportProfiles = new ArrayList<>();
        processes.stream().forEach(
                p -> p.getServiceEndpointList().getEndpoint().stream().forEach(e -> transportProfiles.add(e.getTransportProfile())));

        if (transportProfiles.size() == 0) {
            List<String> processIds = processes.stream().map(p -> p.getProcessIdentifier().getValue()).collect(Collectors.toList());
            throw new ConfigurationException("Metadata for processIds: " + Arrays.toString(processIds.toArray()) + " does not contain transport profile info");
        }

        return transportProfiles;
    }

    /**
     * Returns the available Transport Profile matching the highest ranking priority Security Profile.
     * The Security Profiles priority list is defined in the properties file.
     * If the priority list is not defined the transport profile value defined in the property file will be read.
     *
     * @param transportProfiles list of available transport profiles that are received from the SMP endpoint
     * @return the available Transport Profile matching the highest ranking priority Security Profile
     */
    protected String getAvailableTransportProfileForHighestRankingSecurityProfile(List<String> transportProfiles) {
        List<SecurityProfile> securityProfilesPriorities = getSecurityProfilesPriorityList();
        if (securityProfilesPriorities == null) {
            return getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4);
        }

        //find the Security Profile with the highest priority ranking that matches an available Transport Profile
        SecurityProfile matchingSecurityProfile = securityProfilesPriorities.stream()
                .filter(securityProfile -> transportProfiles.contains(getTransportProfileMatchingSecurityProfile(securityProfile, transportProfiles)))
                .findFirst()
                .orElse(null);

        return getSecurityProfileTransportProfileMap().get(matchingSecurityProfile);
    }

    /**
     * Returns the Transport Profile matching a specific Security Profile according to the SECURITY_PROFILE_TRANSPORT_PROFILE_MAP
     * If no match is found it returns null.
     *
     * @param securityProfile the Security Profile for which the matching Transport Profile is retrieved
     * @param transportProfiles list of available transport profiles returned by SMP
     *
     * @return the matching Security Profile
     */
    protected String getTransportProfileMatchingSecurityProfile(SecurityProfile securityProfile, List<String> transportProfiles) {
        return transportProfiles.stream()
                .filter(transportProfile -> getSecurityProfileTransportProfileMap().get(securityProfile).equalsIgnoreCase(transportProfile))
                .findFirst()
                .orElse(null);
    }

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
