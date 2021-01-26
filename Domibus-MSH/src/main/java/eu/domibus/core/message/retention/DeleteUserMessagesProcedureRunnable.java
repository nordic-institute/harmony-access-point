package eu.domibus.core.message.retention;

import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Date;

/**
 * Class used to delete UserMessages in a new thread
 *
 * @author idragusa
 * @since 4.2.1
 */

public class DeleteUserMessagesProcedureRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeleteUserMessagesProcedureRunnable.class);

    protected UserMessageLogDao userMessageLogDao;
    protected Date startDate;
    protected String mpc;
    protected Integer maxCount;
    protected String queryName;

    public DeleteUserMessagesProcedureRunnable(UserMessageLogDao userMessageLogDao, Date startDate, String mpc, Integer maxCount, String queryName) {
        this.userMessageLogDao = userMessageLogDao;
        this.startDate = startDate;
        this.mpc = mpc;
        this.maxCount = maxCount;
        this.queryName = queryName;
    }

    @Override
    public void run() {
        LOG.debug("Deleting expired messages: startDate [{}], mpc [{}], maxCount [{}], using the stored procedure [{}] ", startDate, mpc, maxCount, queryName);
        userMessageLogDao.deleteExpiredMessages(startDate, mpc, maxCount, queryName);
    }
}
