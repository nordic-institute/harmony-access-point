package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.EbMS3Exception;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
public interface DynamicDiscoveryService {

    EndpointInfo lookupInformation(String lookupCacheKey,
                                   final String finalRecipientValue,
                                   final String finalRecipientType,
                                   final String documentId,
                                   final String processId,
                                   final String processIdScheme) throws EbMS3Exception;

    String getPartyIdType();
    String getResponderRole();

    String getFinalRecipientCacheKeyForDynamicDiscovery(UserMessage userMessage);

}
