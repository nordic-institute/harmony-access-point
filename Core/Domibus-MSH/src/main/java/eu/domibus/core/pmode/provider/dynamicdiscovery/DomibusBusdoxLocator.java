package eu.domibus.core.pmode.provider.dynamicdiscovery;

import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.lookup.api.NotFoundException;
import network.oxalis.vefa.peppol.lookup.locator.AbstractLocator;
import network.oxalis.vefa.peppol.lookup.util.DynamicHostnameGenerator;
import network.oxalis.vefa.peppol.mode.Mode;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;

import java.net.URI;

/**
 * This class was copy/pasted from BusdoxLocator so that we can customize the lookup cache
 *
 * @since 5.0.6
 * @author Cosmin Baciu
 */
public class DomibusBusdoxLocator extends AbstractLocator {

    private DynamicHostnameGenerator hostnameGenerator;

    public DomibusBusdoxLocator(Mode mode) {
        this(
                mode.getString("lookup.locator.busdox.prefix"),
                mode.getString("lookup.locator.hostname"),
                mode.getString("lookup.locator.busdox.algorithm")
        );
    }

    @SuppressWarnings("unused")
    public DomibusBusdoxLocator(String hostname) {
        this("B-", hostname, "MD5");
    }

    public DomibusBusdoxLocator(String prefix, String hostname, String algorithm) {
        hostnameGenerator = new DynamicHostnameGenerator(prefix, hostname, algorithm);
    }

    @Override
    public URI lookup(ParticipantIdentifier participantIdentifier) throws LookupException {
        // Create hostname for participant identifier.
        String hostname = hostnameGenerator.generate(participantIdentifier);

        try {
            final Lookup lookup = new Lookup(hostname);
            //we set the cache to null to avoid DNS cache
            lookup.setCache(null);
            if (lookup.run() == null) {
                if(lookup.getResult() == Lookup.HOST_NOT_FOUND) {
                    throw new NotFoundException(
                            String.format("Identifier '%s' is not registered in SML.", participantIdentifier.getIdentifier()));
                } else {
                    throw new LookupException(
                            String.format("Error when looking up identifier '%s' in SML.", participantIdentifier.getIdentifier()));
                }
            }
        } catch (TextParseException e) {
            throw new LookupException(e.getMessage(), e);
        }

        return URI.create(String.format("http://%s", hostname));
    }
}
