package eu.domibus.ebms3.receiver;

import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
//@thom test this hierarchy of class
public interface LegConfigurationExtractor {
    LegConfiguration extractMessageConfiguration() throws EbMS3Exception;

    public void accept(MessageLegConfigurationVisitor visitor);
}
