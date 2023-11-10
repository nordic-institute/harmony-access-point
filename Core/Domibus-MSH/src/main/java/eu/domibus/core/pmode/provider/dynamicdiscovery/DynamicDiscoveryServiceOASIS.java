package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.core.extension.IExtension;
import eu.europa.ec.dynamicdiscovery.core.extension.impl.oasis10.OasisSMP10Extension;
import eu.europa.ec.dynamicdiscovery.core.extension.impl.oasis20.OasisSMP20Extension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * Service to query a compliant eDelivery SMP profile based on the OASIS BDX Service Metadata Publishers
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
@Qualifier("dynamicDiscoveryServiceOASIS")
public class DynamicDiscoveryServiceOASIS extends AbstractDynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceOASIS.class);

    private static final List<IExtension> DOCUMENT_EXTENSIONS = Arrays.asList(new OasisSMP10Extension(), new OasisSMP20Extension());

    @Override
    protected String getPartyIdTypePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_TYPE;
    }

    @Override
    protected String getPartyIdResponderRolePropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_PARTYID_RESPONDER_ROLE;
    }

    @Override
    protected String getRegexCertificateSubjectValidationPropertyName() {
        return DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION;
    }

    @Override
    protected List<IExtension> getSMPDocumentExtensions() {
        return DOCUMENT_EXTENSIONS;
    }
}
