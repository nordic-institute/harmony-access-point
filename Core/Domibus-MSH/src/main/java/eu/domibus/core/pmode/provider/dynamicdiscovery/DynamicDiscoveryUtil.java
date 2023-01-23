package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.SecurityProfile;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ProcessType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_PROFILE_ORDER;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Utility class for Dynamic Discovery
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class DynamicDiscoveryUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryUtil.class);

    private final DomibusPropertyProvider domibusPropertyProvider;

    public DynamicDiscoveryUtil(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * Return trimmed Domibus property value
     *
     * @param propertyName Domibus property name
     * @return value for the given Domibus property name
     */
    public String getTrimmedDomibusProperty(String propertyName) {
        return trim(domibusPropertyProvider.getProperty(propertyName));
    }

    /**
     * Returns a list of Security Profile names in the priority order specified in the properties file
     *
     * @return a list of ordered Security Profile names
     */
    public List<SecurityProfile> getSecurityProfilesPriorityList() {
        List<String> profilesList = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_SECURITY_PROFILE_ORDER);

        if (profilesList.size() == 0) {
            LOG.warn("The property {} was not specified in the properties file", DOMIBUS_SECURITY_PROFILE_ORDER);
            return null;
        }

        return profilesList.stream()
                .map(SecurityProfile::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Returns the available Transport Profile matching the highest ranking priority Security Profile.
     * The Security Profiles priority list is defined in the properties file.
     * If the priority list is not defined the transport profile value defined in the property file will be read.
     *
     * @param transportProfiles list of available transport profiles that are received from the SMP endpoint
     * @param securityProfileTransportProfileMap mapping between Security Profiles and Transport Profiles
     * @return the available Transport Profile matching the highest ranking priority Security Profile
     */
    public String getAvailableTransportProfileForHighestRankingSecurityProfile(List<String> transportProfiles,
                                                                               Map<SecurityProfile, String> securityProfileTransportProfileMap) {
        List<SecurityProfile> securityProfilesPriorities = getSecurityProfilesPriorityList();
        if (securityProfilesPriorities == null) {
            return getTrimmedDomibusProperty(DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4);
        }

        //find the Security Profile with the highest priority ranking that matches an available Transport Profile
        SecurityProfile matchingSecurityProfile = securityProfilesPriorities.stream()
                .filter(securityProfile -> transportProfiles.contains(getTransportProfileMatchingSecurityProfile(securityProfile, transportProfiles, securityProfileTransportProfileMap)))
                .findFirst()
                .orElse(null);

        return securityProfileTransportProfileMap.get(matchingSecurityProfile);
    }

    /**
     * Returns the Transport Profile matching a specific Security Profile according to the SECURITY_PROFILE_TRANSPORT_PROFILE_MAP
     * If no match is found it returns null.
     *
     * @param securityProfile the Security Profile for which the matching Transport Profile is retrieved
     * @param transportProfiles list of available transport profiles returned by SMP
     * @param securityProfileTransportProfileMap mapping between Security Profiles and Transport Profiles
     *
     * @return the matching Security Profile
     */
    public String getTransportProfileMatchingSecurityProfile(SecurityProfile securityProfile,
                                                             List<String> transportProfiles,
                                                             Map<SecurityProfile, String> securityProfileTransportProfileMap) {
        return transportProfiles.stream()
                .filter(transportProfile -> securityProfileTransportProfileMap.get(securityProfile).equalsIgnoreCase(transportProfile))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the list of Transport Profiles that are extracted from the Processes list
     *
     * @return a list of available Transport Profiles
     */
    public List<String> retrieveTransportProfilesFromProcesses(List<ProcessType> processes) {
        List<String> transportProfiles = new ArrayList<>();
        processes.stream().forEach(
                p -> p.getServiceEndpointList().getEndpoint().stream().forEach(e -> transportProfiles.add(e.getTransportProfile())));

        return transportProfiles;
    }

}
