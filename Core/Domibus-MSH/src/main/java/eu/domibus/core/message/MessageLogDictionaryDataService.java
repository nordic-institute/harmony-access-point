package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.time.TimezoneOffsetDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static eu.domibus.core.message.MessageLogInfoFilter.*;
import static eu.domibus.web.rest.MessageLogResource.*;

/**
 * @author Ion Perpegel
 * @since 5.0.7
 */
@Service
public class MessageLogDictionaryDataService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLogDictionaryDataService.class);

    private final MessageStatusDao messageStatusDao;

    private final MshRoleDao mshRoleDao;

    private final NotificationStatusDao notificationStatusDao;

    private final ActionDao actionDao;

    private final ServiceDao serviceDao;

    private final PartyIdDao partyIdDao;

    private final TimezoneOffsetDao timezoneOffsetDao;

    public MessageLogDictionaryDataService(MessageStatusDao messageStatusDao, MshRoleDao mshRoleDao,
                                           NotificationStatusDao notificationStatusDao, ActionDao actionDao,
                                           ServiceDao serviceDao, PartyIdDao partyIdDao, TimezoneOffsetDao timezoneOffsetDao) {
        this.messageStatusDao = messageStatusDao;
        this.mshRoleDao = mshRoleDao;
        this.notificationStatusDao = notificationStatusDao;
        this.actionDao = actionDao;
        this.serviceDao = serviceDao;
        this.partyIdDao = partyIdDao;
        this.timezoneOffsetDao = timezoneOffsetDao;
    }

    public void addDictionaryData(List<String> fields, MessageLogInfo msgInfo) {
        if (fields.contains(PROPERTY_MESSAGE_STATUS)) {
            addMessageStatus(msgInfo);
        }

        if (fields.contains(PROPERTY_MSH_ROLE)) {
            addMshRole(msgInfo);
        }

        if (fields.contains(PROPERTY_NOTIFICATION_STATUS)) {
            addNotificationStatus(msgInfo);
        }

        if (fields.contains(MESSAGE_ACTION)) {
            addAction(msgInfo);
        }

        if ((fields.contains(MESSAGE_SERVICE_TYPE) || fields.contains(MESSAGE_SERVICE_VALUE))) {
            addService(fields, msgInfo);
        }

        if (fields.contains(PROPERTY_FROM_PARTY_ID)) {
            addFromParty(msgInfo);
        }

        if (fields.contains(PROPERTY_TO_PARTY_ID)) {
            addToParty(msgInfo);
        }

        addTimeZoneOffset(msgInfo);
    }

    private void addTimeZoneOffset(MessageLogInfo msgInfo) {
        Long nextAttemptTimezonePk = msgInfo.getNextAttemptTimezonePk();
        if (nextAttemptTimezonePk == null) {
            LOG.debug("TimezoneOffset id is null.");
            return;
        }
        LOG.debug("Adding TimezoneOffset.");
        TimezoneOffset entity = timezoneOffsetDao.read(nextAttemptTimezonePk);
        if (entity == null) {
            LOG.warn("Could not find TimezoneOffset with id [{}]", nextAttemptTimezonePk);
            return;
        }
        msgInfo.setNextAttemptTimezoneId(entity.getNextAttemptTimezoneId());
        msgInfo.setNextAttemptOffsetSeconds(entity.getNextAttemptOffsetSeconds());
    }

    private void addToParty(MessageLogInfo msgInfo) {
        Long toPartyIdPk = msgInfo.getToPartyIdPk();
        if (toPartyIdPk == null || toPartyIdPk <= 0) {
            LOG.debug("ToPartyIdPk is null");
            return;
        }
        LOG.debug("Adding ToPartyIdPk.");
        PartyId entity = partyIdDao.read(toPartyIdPk);
        if (entity == null) {
            LOG.warn("Could not find PartyId with id [{}]", toPartyIdPk);
            return;
        }
        msgInfo.setToPartyId(entity.getValue());
    }

    private void addFromParty(MessageLogInfo msgInfo) {
        Long fromPartyIdPk = msgInfo.getFromPartyIdPk();
        if (fromPartyIdPk == null || fromPartyIdPk <= 0) {
            LOG.debug("FromPartyIdPk is null");
            return;
        }
        LOG.debug("Adding FromPartyIdPk.");
        PartyId entity = partyIdDao.read(fromPartyIdPk);
        if (entity == null) {
            LOG.warn("Could not find PartyId with id [{}]", fromPartyIdPk);
            return;
        }
        msgInfo.setFromPartyId(entity.getValue());
    }

    private void addService(List<String> fields, MessageLogInfo msgInfo) {
        Long serviceId = msgInfo.getServiceId();
        if (serviceId == null || serviceId <= 0) {
            LOG.debug("ServiceId is null");
            return;
        }
        LOG.debug("Adding ServiceId.");
        ServiceEntity entity = serviceDao.read(serviceId);
        if (entity == null) {
            LOG.warn("Could not find ServiceEntity with id [{}]", serviceId);
            return;
        }
        if (fields.contains(MESSAGE_SERVICE_TYPE)) {
            msgInfo.setServiceType(entity.getType());
        }
        if (fields.contains(MESSAGE_SERVICE_VALUE)) {
            msgInfo.setServiceValue(entity.getValue());
        }
    }

    private void addAction(MessageLogInfo msgInfo) {
        Long actionId = msgInfo.getActionId();
        if (actionId == null || actionId <= 0) {
            LOG.debug("ActionId is null");
            return;
        }
        LOG.debug("Adding ActionId.");
        ActionEntity entity = actionDao.read(actionId);
        if (entity == null) {
            LOG.warn("Could not find ActionEntity with id [{}]", actionId);
            return;
        }
        msgInfo.setAction(entity.getValue());
    }

    private void addNotificationStatus(MessageLogInfo msgInfo) {
        Long notificationStatusId = msgInfo.getNotificationStatusId();
        if (notificationStatusId == null || notificationStatusId <= 0) {
            LOG.debug("NotificationStatusId is null");
            return;
        }
        LOG.debug("Adding NotificationStatusId.");
        NotificationStatusEntity entity = notificationStatusDao.read(notificationStatusId);
        if (entity == null) {
            LOG.warn("Could not find NotificationStatusEntity with id [{}]", notificationStatusId);
            return;
        }
        msgInfo.setNotificationStatus(entity.getStatus());
    }

    private void addMshRole(MessageLogInfo msgInfo) {
        Long mshRoleId = msgInfo.getMshRoleId();
        if (mshRoleId == null || mshRoleId <= 0) {
            LOG.debug("MshRoleId is null");
            return;
        }
        LOG.debug("Adding MshRoleId.");
        MSHRoleEntity entity = mshRoleDao.read(mshRoleId);
        if (entity == null) {
            LOG.warn("Could not find MSHRoleEntity with id [{}]", mshRoleId);
            return;
        }
        msgInfo.setMshRole(entity.getRole());
    }

    private void addMessageStatus(MessageLogInfo msgInfo) {
        Long messageStatusId = msgInfo.getMessageStatusId();
        if (messageStatusId == null || messageStatusId <= 0) {
            LOG.debug("MessageStatusId is null");
            return;
        }
        LOG.debug("Adding MessageStatusId.");
        MessageStatusEntity entity = messageStatusDao.read(messageStatusId);
        if (entity == null) {
            LOG.warn("Could not find MessageStatusEntity with id [{}]", messageStatusId);
            return;
        }
        msgInfo.setMessageStatus(entity.getMessageStatus());
    }

}
