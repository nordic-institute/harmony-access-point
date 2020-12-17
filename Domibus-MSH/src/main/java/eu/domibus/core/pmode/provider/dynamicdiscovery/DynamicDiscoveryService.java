package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.core.ebms3.EbMS3Exception;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
public interface DynamicDiscoveryService {
    String SMLZONE_KEY = DOMIBUS_SMLZONE;
    String DYNAMIC_DISCOVERY_TRANSPORTPROFILEAS4 = DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4;
    String DYNAMIC_DISCOVERY_MODE = DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE;
    String DYNAMIC_DISCOVERY_CERT_REGEX = DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION;
    String DYNAMIC_DISCOVERY_CERT_PEPPOL_REGEX = DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION;
    // TODO Split the following two properties into different properties, one per specification (i.e. OASIS or PEPPOL),
    //  in order to be able to define default values in the  property files and remove the hardcoded ones
    String DYNAMIC_DISCOVERY_PARTYID_RESPONDER_ROLE = DOMIBUS_DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE;
    String DYNAMIC_DISCOVERY_PARTYID_TYPE = DOMIBUS_DYNAMICDISCOVERY_PARTYID_TYPE;
    String USE_DYNAMIC_DISCOVERY = DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;

    EndpointInfo lookupInformation(final String domain,
                                   final String participantId,
                                   final String participantIdScheme,
                                   final String documentId,
                                   final String processId,
                                   final String processIdScheme) throws EbMS3Exception;

    String getPartyIdType();
    String getResponderRole();
}