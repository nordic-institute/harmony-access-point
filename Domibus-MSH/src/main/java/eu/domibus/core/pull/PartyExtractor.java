package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 * <p>
 * {@inheritDoc}
 */
public class PartyExtractor implements PartyIdExtractor {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PartyExtractor.class);

    private final Party party;

    private final Collection<PartyId> partyIds;

    public PartyExtractor(final Party party, Collection<PartyId> partyIds) {
        this.party = party;
        this.partyIds = partyIds;
    }

    /**
     * @return the first identifier of the list. Throw IllegalStateException if none exists.
     */
    @Override
    public String getPartyId() {

        Set<Identifier> identifiers = party.getIdentifiers();
        if (identifiers.isEmpty()) {
            LOG.warn("No identifier defined for party:[{}], the message will not be available for pulling", party.getName());
            throw new IllegalStateException("Party should have an identifier");
        }
        for (Identifier identifier : identifiers) {
            String pmodePartyIdValue = identifier.getPartyId();
            String pmodePartyIdTypeValue = null;
            PartyIdType partyIdType = identifier.getPartyIdType();
            if (partyIdType != null) {
                pmodePartyIdTypeValue = partyIdType.getValue();
            }
            for (PartyId partyId : partyIds) {
                String messagePartyIdValue = partyId.getValue();
                String messagePartyIdTypeValue = partyId.getType();
                LOG.debug("comparing pmode party id:[{}] with message party id[{}] and pmode party id type:[{}] with message party id type:[{}]", pmodePartyIdValue, messagePartyIdValue, pmodePartyIdTypeValue, messagePartyIdTypeValue);
                if (StringUtils.compareIgnoreCase(pmodePartyIdValue, messagePartyIdValue) == 0 && StringUtils.compareIgnoreCase(pmodePartyIdTypeValue, messagePartyIdTypeValue) == 0) {
                    return pmodePartyIdValue;
                }
            }
        }
        throw new IllegalStateException("No mapping between pmode party and message party");
    }
}
