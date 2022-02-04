package eu.domibus.core.ebms3.sender;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * Class responsible for sending AS4 UserMessages to C3
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UserMessageSender extends AbstractUserMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageSender.class);

    protected PartInfoDao partInfoDao;

    public UserMessageSender(PartInfoDao partInfoDao) {
        this.partInfoDao = partInfoDao;
    }

    @Override
    protected SOAPMessage createSOAPMessage(UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception {
        List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
        return messageBuilder.buildSOAPMessage(userMessage, partInfos, legConfiguration);
    }

    @Override
    protected DomibusLogger getLog() {
        return LOG;
    }
}
