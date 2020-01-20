package eu.domibus.performancetest;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;


@Repository
public class TestMessageDao extends BasicDao<TestMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestMessageDao.class);

    public TestMessageDao() {
        super(TestMessage.class);
    }
}
