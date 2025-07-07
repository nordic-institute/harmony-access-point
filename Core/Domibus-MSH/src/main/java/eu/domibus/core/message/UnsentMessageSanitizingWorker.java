package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.model.ProcessingType;
import eu.domibus.api.model.UserMessageLogDto;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * A worker that picks up unsent messages that are still in ${@code SEND_ENQUEUED} and ${@code WAITING_FOR_RETRY} states
 * and tries to dispatch them again. Recent messages and recently restored messages are ignored by specifying
 * an interval in minutes that should be ignored when looking up for stuck messages.
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

    @Autowired
    private EventService eventService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("UnsentMessageSanitizingWorker to be executed");
        authUtils.runWithSecurityContext(this::sanitize, "unsent_user", "unsent_password");
    }

    protected void sanitize() {
        int ignoreMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES);
        LOG.debug("Checking for unsent messages that have been stuck for more than [{}] minutes", ignoreMinutes);

        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout(ProcessingType.PUSH);
        if (maxRetryTimeout < 0) {
            LOG.debug("There is no retry configured in PMode for PUSH processing type, skip sanitizing attempt");
            return;
        }
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

        // check the SendRetryWorker properties and log WARN if any apparent overlap
        int sendRetryTimeoutDelay = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MSH_RETRY_TIMEOUT_DELAY);
        long minEntityIdOfSendRetryWorker = dateUtil.getMinEntityId(MINUTES.toSeconds(maxRetryTimeout + sendRetryTimeoutDelay));
        if (minEntityIdOfSendRetryWorker <= maxEntityId) {
            // minEntityIdOfSendRetryWorker is the min entity id that the SendRetryWorker would process if started at the same moment;
            // this does not cover the case when the SendRetryWorker is already running (and runs for an unexpectedly long time),
            // but it is an indication that the two workers may overlap
            LOG.warn("The [{}] property value [{}] overlaps with the [{}] property value [{}]. " +
                    "The SendRetryWorker will process messages with entity ids greater than [{}]. " +
                    "The UnsentMessageSanitizingWorker will process messages with entity ids lower than [{}]. " +
                    "This may cause messages to be retried by both the SendRetryWorker and the UnsentMessageSanitizingWorker.",
                    DOMIBUS_MESSAGES_STUCK_IGNORE_RECENT_MINUTES, ignoreMinutes, DOMIBUS_MSH_RETRY_TIMEOUT_DELAY, sendRetryTimeoutDelay,
                    minEntityIdOfSendRetryWorker, maxEntityId);
        }

        int maxMessageCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGES_STUCK_MAX_COUNT);
        if (maxMessageCount == 0) {
            // no messages will be retried; an alert will be raised instead
            long count = userMessageLogDao.countUnsentMessages(minutesAgo, maxEntityId);
            if (count == 0) {
                LOG.debug("No stuck unsent messages found before [{}]", minutesAgo);
                return;
            }

            LOG.info("Found [{}] unsent stuck messages before [{}], threshold id: [{}]", count, minutesAgo, maxEntityId);
            eventService.enqueueEvent(EventType.OLD_ONGOING_MESSAGES, "" + maxEntityId, new EventProperties(count, minutesAgo));
            return;
        }

        List<UserMessageLogDto> unsentMessageDtos = userMessageLogDao.findUnsentMessageIds(minutesAgo, maxEntityId, maxMessageCount);

        if (CollectionUtils.isEmpty(unsentMessageDtos)) {
            LOG.debug("No unsent stuck messages found to dispatch (threshold date: [{}], threshold id: [{}])", minutesAgo, maxEntityId);
            return;
        }
        List<String> skippedMessageIds = new ArrayList<>();
        LOG.info("Preparing [{}] unsent stuck messages for dispatch (threshold date: [{}], threshold id: [{}])", unsentMessageDtos.size(), minutesAgo, maxEntityId);
        LOG.trace("Unsent messages {}", unsentMessageDtos);

        for (UserMessageLogDto unsentMessageDto : unsentMessageDtos) {
            try {
                userMessageService.sendEnqueuedMessage(unsentMessageDto.getMessageId(), unsentMessageDto.getEntityId());
            } catch (UserMessageException e) {
                skippedMessageIds.add(unsentMessageDto.getMessageId());
                LOG.trace("UserMessage [{}] with entityId [{}] skipped", unsentMessageDto.getMessageId(), unsentMessageDto.getEntityId(), e);
            }
        }
        if (!isEmpty(skippedMessageIds)) {
            LOG.info("[{}]/[{}] messages skipped due to them being already unstuck by a different process: {}", skippedMessageIds.size(), unsentMessageDtos.size(), skippedMessageIds);
        }
    }
}
