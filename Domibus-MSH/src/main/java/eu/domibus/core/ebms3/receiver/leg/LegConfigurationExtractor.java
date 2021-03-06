package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
//@thom test this hierarchy of class
public interface LegConfigurationExtractor {
    LegConfiguration extractMessageConfiguration() throws EbMS3Exception;

    public void accept(MessageLegConfigurationVisitor visitor);
}
