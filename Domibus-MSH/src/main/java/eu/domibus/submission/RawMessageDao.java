package eu.domibus.submission;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;


@Repository
public class RawMessageDao extends BasicDao<RawMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawMessageDao.class);

    public RawMessageDao() {
        super(RawMessage.class);
    }
}
