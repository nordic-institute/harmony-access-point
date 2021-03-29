package eu.domibus.core.message.acknowledge;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

/**Cosmin Baciu
 * @since 5.0
 */
@Repository
public class MessageAcknowledgementPropertyDao extends BasicDao<MessageAcknowledgementProperty> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementPropertyDao.class);

    public MessageAcknowledgementPropertyDao() {
        super(MessageAcknowledgementProperty.class);
    }

}
