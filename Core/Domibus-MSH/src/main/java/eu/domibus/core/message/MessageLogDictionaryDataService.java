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
        if (msgInfo.getNextAttemptTimezonePk() == null) {
            LOG.debug("TimezoneOffset id is null.");
            return;
        }
        LOG.debug("Adding TimezoneOffset.");
        TimezoneOffset entity = timezoneOffsetDao.findByReference(msgInfo.getNextAttemptTimezonePk());
        if (entity == null) {
            LOG.warn("Could not find TimezoneOffset with id [{}]", msgInfo.getNextAttemptTimezonePk());
            return;
        }
        msgInfo.setNextAttemptTimezoneId(entity.getNextAttemptTimezoneId());
        msgInfo.setNextAttemptOffsetSeconds(entity.getNextAttemptOffsetSeconds());
    }

    private void addToParty(MessageLogInfo msgInfo) {
        if (msgInfo.getToPartyIdPk() <= 0) {
            LOG.debug("ToPartyIdPk is null");
            return;
        }
        LOG.debug("Adding ToPartyIdPk.");
        PartyId entity = partyIdDao.findByReference(msgInfo.getToPartyIdPk());
        if (entity == null) {
            LOG.warn("Could not find PartyId with id [{}]", msgInfo.getToPartyIdPk());
            return;
        }
        msgInfo.setToPartyId(entity.getValue());
    }

    private void addFromParty(MessageLogInfo msgInfo) {
        if (msgInfo.getFromPartyIdPk() <= 0) {
            LOG.debug("FromPartyIdPk is null");
            return;
        }
        LOG.debug("Adding FromPartyIdPk.");
        PartyId entity = partyIdDao.findByReference(msgInfo.getFromPartyIdPk());
        if (entity == null) {
            LOG.warn("Could not find PartyId with id [{}]", msgInfo.getFromPartyIdPk());
            return;
        }
        msgInfo.setFromPartyId(entity.getValue());
    }

    private void addService(List<String> fields, MessageLogInfo msgInfo) {
        if (msgInfo.getServiceId() <= 0) {
            LOG.debug("ServiceId is null");
            return;
        }
        LOG.debug("Adding ServiceId.");
        ServiceEntity entity = serviceDao.findByReference(msgInfo.getServiceId());
        if (entity == null) {
            LOG.warn("Could not find ServiceEntity with id [{}]", msgInfo.getServiceId());
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
        if (msgInfo.getActionId() <= 0) {
            LOG.debug("ActionId is null");
            return;
        }
        LOG.debug("Adding ActionId.");
        ActionEntity entity = actionDao.findByReference(msgInfo.getActionId());
        if (entity == null) {
            LOG.warn("Could not find ActionEntity with id [{}]", msgInfo.getActionId());
            return;
        }
        msgInfo.setAction(entity.getValue());
    }

    private void addNotificationStatus(MessageLogInfo msgInfo) {
        if (msgInfo.getNotificationStatusId() <= 0) {
            LOG.debug("NotificationStatusId is null");
            return;
        }
        LOG.debug("Adding NotificationStatusId.");
        NotificationStatusEntity entity = notificationStatusDao.findByReference(msgInfo.getNotificationStatusId());
        if (entity == null) {
            LOG.warn("Could not find NotificationStatusEntity with id [{}]", msgInfo.getNotificationStatusId());
            return;
        }
        msgInfo.setNotificationStatus(entity.getStatus());
    }

    private void addMshRole(MessageLogInfo msgInfo) {
        if (msgInfo.getMshRoleId() <= 0) {
            LOG.debug("MshRoleId is null");
            return;
        }
        LOG.debug("Adding MshRoleId.");
        MSHRoleEntity entity = mshRoleDao.findByReference(msgInfo.getMshRoleId());
        if (entity == null) {
            LOG.warn("Could not find MSHRoleEntity with id [{}]", msgInfo.getMshRoleId());
            return;
        }
        msgInfo.setMshRole(entity.getRole());
    }

    private void addMessageStatus(MessageLogInfo msgInfo) {
        if (msgInfo.getMessageStatusId() <= 0) {
            LOG.debug("MessageStatusId is null");
            return;
        }
        LOG.debug("Adding MessageStatusId.");
        MessageStatusEntity entity = messageStatusDao.findByReference(msgInfo.getMessageStatusId());
        if (entity == null) {
            LOG.warn("Could not find MessageStatusEntity with id [{}]", msgInfo.getMessageStatusId());
            return;
        }
        msgInfo.setMessageStatus(entity.getMessageStatus());
    }

}
