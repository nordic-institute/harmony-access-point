package eu.domibus.core.message.validation;

import eu.domibus.core.spi.validation.UserMessageValidatorSpiException;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class UserMessageValidatorSpiMock implements UserMessageValidatorSpi {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageValidatorSpiMock.class);

    @Override
    public void validateUserMessage(UserMessageDTO userMessage) throws UserMessageValidatorSpiException {
        final Set<PartInfoDTO> partInfo = userMessage.getPayloadInfo().getPartInfo();

        for (PartInfoDTO partInfoDTO : partInfo) {
            try {
                LOG.debug("Consuming part info [{}]", partInfoDTO.getHref());
                //consume the data handler
                IOUtils.toByteArray(partInfoDTO.getPayloadDatahandler().getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) throws UserMessageValidatorSpiException {

    }
}
