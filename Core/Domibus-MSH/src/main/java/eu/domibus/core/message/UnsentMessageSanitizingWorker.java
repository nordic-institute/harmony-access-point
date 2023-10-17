package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * A worker that picks up unsent messages that are still in ${@code SEND_ENQUEUED} and ${@code WAITING_FOR_RETRY} states
 * and tries to dispatch them again. Recent messages are ignored by specifying an interval in minutes that should be
 * ignored when looking up for stuck messages.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0.7
 */
@DisallowConcurrentExecution
public class UnsentMessageSanitizingWorker extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UnsentMessageSanitizingWorker.class);

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UserMessageDefaultService userMessageService;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private PModeProvider pModeProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("SendEnqueuedMessageSanitizingWorker to be executed");
        authUtils.runWithSecurityContext(this::sanitize, "unsent_user", "unsent_password");
    }

    protected void sanitize() {
        int ignoreMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);
        LOG.debug("Checking for scheduled messages that have been stuck for more than [{}] minutes", ignoreMinutes);

        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout();
        int retryIgnoreMinutes = maxRetryTimeout + ignoreMinutes;
        LOG.debug("Checking for retry messages that have been stuck for more than [{}] minutes", retryIgnoreMinutes);

        Date minutesAgo;
        try {
            minutesAgo = dateUtil.getDateMinutesAgo(ignoreMinutes);
        } catch (DomibusDateTimeException e) {
            LOG.error("Please use only positive values greater than 0 for the [{}] property", DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES, e);
            return;
        }

        long maxEntityId = dateUtil.getMaxEntityId(MINUTES.toSeconds(retryIgnoreMinutes));
        List<String> unsentMessageIds = userMessageLogDao.findUnsentMessageIds(minutesAgo, maxEntityId);

        if (unsentMessageIds == null || unsentMessageIds.isEmpty()) {
            LOG.debug("No unsent stuck messages found to dispatch");
            return;
        }

        LOG.info("Prepare unsent messages for dispatch {}", unsentMessageIds);
        unsentMessageIds.forEach(userMessageService::sendEnqueuedMessage);
    }
}
