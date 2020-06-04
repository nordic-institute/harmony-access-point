
package eu.domibus.core.generator.id;

import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MSH_MESSAGEID_SUFFIX;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service("messageIdGenerator")
public class MessageIdGenerator {
    private static final String MESSAGE_ID_SUFFIX_PROPERTY = DOMIBUS_MSH_MESSAGEID_SUFFIX;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected NoArgGenerator uuidGenerator;

    public String generateMessageId() {
        String messageIdSuffix = domibusPropertyProvider.getProperty(MESSAGE_ID_SUFFIX_PROPERTY);
        return uuidGenerator.generate() + "@" + messageIdSuffix;
    }
}
