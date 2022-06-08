package eu.domibus.plugin.ws.backend;

import eu.domibus.plugin.ws.AbstractBackendWSIT;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static java.time.LocalDateTime.of;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class WSBackendMessageLogDaoIT extends AbstractBackendWSIT {

    @Autowired
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    private WSBackendMessageLogEntity entityFailed2021;
    private WSBackendMessageLogEntity entityFailed2022;

    private WSBackendMessageLogEntity entityRetried1;

    private WSBackendMessageLogEntity entityRetried2;

    @Before
    public void setUp() {
        entityFailed2021 = create(WSBackendMessageStatus.SEND_FAILURE,
                of(2021, 12, 31, 1, 1),
                "SENDER1",
                "BACKEND1");
        entityFailed2022 = create(WSBackendMessageStatus.SEND_FAILURE,
                of(2022, 5, 10, 1, 1),
                "SENDER2",
                "BACKEND2");
        entityRetried1 = create(WSBackendMessageStatus.WAITING_FOR_RETRY);
        entityRetried2 = create(WSBackendMessageStatus.WAITING_FOR_RETRY);
        createEntityAndFlush(Arrays.asList(entityFailed2021,
                entityFailed2022,
                entityRetried1,
                entityRetried2));
    }

    @Test
    public void findByMessageId_notFound() {
        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId("");
        Assert.assertNull(byMessageId);
    }

    @Test
    public void findByMessageId_findOne() {
        WSBackendMessageLogEntity byMessageId = wsBackendMessageLogDao.findByMessageId(entityFailed2021.getMessageId());
        Assert.assertNotNull(byMessageId);
    }

    @Test
    public void findRetryMessages() {
        List<WSBackendMessageLogEntity> messages = wsBackendMessageLogDao.findRetryMessages();
        Assert.assertNotNull(messages);
        Assert.assertEquals(2, messages.size());
        MatcherAssert.assertThat(messages, CoreMatchers.hasItems(entityRetried1, entityRetried2));
    }

    @Test
    public void findAllFailedWithFilter() {
        List<WSBackendMessageLogEntity> allFailedWithFilter =
                wsBackendMessageLogDao.findAllFailedWithFilter(null, null, null, null, null, 5);
        MatcherAssert.assertThat(allFailedWithFilter.size(), Is.is(2));
        MatcherAssert.assertThat(allFailedWithFilter, CoreMatchers.hasItems(entityFailed2021, entityFailed2022));
    }

    @Test
    public void findAllFailedWithFilter_DateFrom() {
        List<WSBackendMessageLogEntity> allFailedWithFilter =
                wsBackendMessageLogDao.findAllFailedWithFilter(null, null, null, of(2022, 1, 1, 1, 1, 1), null, 5);
        MatcherAssert.assertThat(allFailedWithFilter.size(), Is.is(1));
        MatcherAssert.assertThat(allFailedWithFilter, CoreMatchers.hasItems(entityFailed2022));
    }

    @Test
    public void findAllFailedWithFilter_DateTo() {
        List<WSBackendMessageLogEntity> allFailedWithFilter =
                wsBackendMessageLogDao.findAllFailedWithFilter(null, null, null, null, of(2022, 1, 1, 1, 1, 1), 5);
        MatcherAssert.assertThat(allFailedWithFilter.size(), Is.is(1));
        MatcherAssert.assertThat(allFailedWithFilter, CoreMatchers.hasItems(entityFailed2021));
    }

    @Test
    public void findAllFailedWithFilter_Sender() {
        List<WSBackendMessageLogEntity> allFailedWithFilter =
                wsBackendMessageLogDao.findAllFailedWithFilter(null, "SENDER1", null, null, null, 5);
        MatcherAssert.assertThat(allFailedWithFilter.size(), Is.is(1));
        MatcherAssert.assertThat(allFailedWithFilter, CoreMatchers.hasItems(entityFailed2021));
    }

    @Test
    public void findAllFailedWithFilter_FinalRecipient() {
        List<WSBackendMessageLogEntity> allFailedWithFilter =
                wsBackendMessageLogDao.findAllFailedWithFilter(null, null, "BACKEND1", null, null, 5);
        MatcherAssert.assertThat(allFailedWithFilter.size(), Is.is(1));
        MatcherAssert.assertThat(allFailedWithFilter, CoreMatchers.hasItems(entityFailed2021));
    }
}