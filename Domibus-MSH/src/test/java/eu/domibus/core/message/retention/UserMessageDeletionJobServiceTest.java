package eu.domibus.core.message.retention;

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
public class UserMessageDeletionJobServiceTest {

    @Injectable
    protected UserMessageDeletionJobDao userMessageDeletionJobDao;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Tested
    protected UserMessageDeletionJobService userMessageDeletionJobService;

    @Test
    public void testJobsOverlap() {
        Date startDate = DateUtils.addMinutes(new Date(), 0);
        Date endDate = DateUtils.addMinutes(new Date(), 24*60);
        UserMessageDeletionJob deletionJob1 = new UserMessageDeletionJob("mpc1", startDate, endDate, 5000, "procName" );
        startDate = DateUtils.addMinutes(new Date(), 24*60*-1);
        endDate = DateUtils.addMinutes(new Date(), 10);
        UserMessageDeletionJob deletionJob2 = new UserMessageDeletionJob("mpc1", startDate, endDate, 5000, "procName" );

        Assert.assertTrue(userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2));
    }

    @Test
    public void testDifferentJobsDoNotOverlap() {
        UserMessageDeletionJob deletionJob1 = new UserMessageDeletionJob("mpc1", new Date(), new Date(), 5000, "procName" );
        UserMessageDeletionJob deletionJob2 = new UserMessageDeletionJob("mpc2", new Date(), new Date(), 5000, "procName" );

        Assert.assertFalse(userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2));
    }

    @Test
    public void testDSameJobsDoNotOverlap() {
        Date startDate = DateUtils.addMinutes(new Date(), 0);
        Date endDate = DateUtils.addMinutes(new Date(), 24*60);
        UserMessageDeletionJob deletionJob1 = new UserMessageDeletionJob("mpc1", startDate, endDate, 5000, "procName" );
        startDate = DateUtils.addMinutes(new Date(), 24*60*-1);
        endDate = DateUtils.addMinutes(new Date(), 0);
        UserMessageDeletionJob deletionJob2 = new UserMessageDeletionJob("mpc1", startDate, endDate, 5000, "procName" );
        userMessageDeletionJobService.doJobsOverlap(deletionJob1, deletionJob2);
    }
}
