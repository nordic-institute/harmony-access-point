package eu.domibus.core.message.retention;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.core.message.UserMessageLogDao;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * @author idragusa
 * @since 4.2.1
 */
@RunWith(JMockit.class)
public class UserMessageDeletionJobEntityServiceTest {

    @Injectable
    protected UserMessageDeletionJobDao userMessageDeletionJobDao;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    MetricRegistry metricRegistry;

    @Tested
    protected UserMessageDeletionJobService userMessageDeletionJobService;

    @Test
    public void testJobsOverlap() {
        Date startDate = DateUtils.addMinutes(new Date(), 0);
        Date endDate = DateUtils.addMinutes(new Date(), 24*60);
        UserMessageDeletionJobEntity deletionJob1 = new UserMessageDeletionJobEntity("mpc1", startDate, endDate, 5000, "procName", 1 );
        startDate = DateUtils.addMinutes(new Date(), 24*60*-1);
        endDate = DateUtils.addMinutes(new Date(), 10);
        UserMessageDeletionJobEntity deletionJob2 = new UserMessageDeletionJobEntity("mpc1", startDate, endDate, 5000, "procName", 2 );

        Assert.assertTrue(userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2));
    }

    @Test
    public void testDifferentJobsDoNotOverlap() {
        UserMessageDeletionJobEntity deletionJob1 = new UserMessageDeletionJobEntity("mpc1", new Date(), new Date(), 5000, "procName", 1 );
        UserMessageDeletionJobEntity deletionJob2 = new UserMessageDeletionJobEntity("mpc2", new Date(), new Date(), 5000, "procName", 2 );

        Assert.assertFalse(userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2));
    }

    @Test
    public void testDSameJobsDoNotOverlap() {
        Date startDate = DateUtils.addMinutes(new Date(), 0);
        Date endDate = DateUtils.addMinutes(new Date(), 24*60);
        UserMessageDeletionJobEntity deletionJob1 = new UserMessageDeletionJobEntity("mpc1", startDate, endDate, 5000, "procName", 1 );
        startDate = DateUtils.addMinutes(new Date(), 24*60*-1);
        endDate = DateUtils.addMinutes(new Date(), 0);
        UserMessageDeletionJobEntity deletionJob2 = new UserMessageDeletionJobEntity("mpc1", startDate, endDate, 5000, "procName", 2 );
        userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2);
    }
}
