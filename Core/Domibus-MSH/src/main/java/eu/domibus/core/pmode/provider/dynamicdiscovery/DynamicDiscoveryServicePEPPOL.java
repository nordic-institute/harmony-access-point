package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.core.extension.IExtension;
import eu.europa.ec.dynamicdiscovery.core.extension.impl.peppol.PeppolSMPExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Service to query a compliant eDelivery SMP profile based on the PeppolService Metadata Publishers
 * (SMP) to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 * The End Receiver Participant ID (C4)
 * The Document ID
 * The Process ID
 * <p>
 * Upon a successful lookup, the result contains the endpoint address and also the public
 * certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServicePEPPOL")
public class DynamicDiscoveryServicePEPPOL extends AbstractDynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServicePEPPOL.class);

    private static final List<IExtension> DOCUMENT_EXTENSIONS = Arrays.asList(new PeppolSMPExtension());

    @Override
    protected String getPartyIdTypePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_TYPE;
    }

    @Override
    protected String getPartyIdResponderRolePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_PARTYID_RESPONDER_ROLE;
    }

    @Override
    protected String getRegexCertificateSubjectValidationPropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION;
    }

    @Override
    protected List<IExtension> getSMPDocumentExtensions() {
        return DOCUMENT_EXTENSIONS;
    }
}
