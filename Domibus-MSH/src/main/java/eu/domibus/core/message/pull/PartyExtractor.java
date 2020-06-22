package eu.domibus.core.message.pull;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 *
 * {@inheritDoc}
 *
 */
public class PartyExtractor implements PartyIdExtractor {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(PartyExtractor.class);

    private final Party party;

    public PartyExtractor(final Party party) {
        this.party = party;
    }

    /**
     * @return the first identifier of the list. Throw IllegalStateException if none exists.
     */
    @Override
    public String getPartyId() {
        List<Identifier> identifiers = party.getIdentifiers();
        if (identifiers.size() == 0) {
            LOG.warn("No identifier defined for party:[{}], the message will not be available for pulling", party.getName());
            throw new IllegalStateException("Party should have an identifier");
        }
        Identifier identifier = identifiers.iterator().next();
        return identifier.getPartyId();
    }
}
