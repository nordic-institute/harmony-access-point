package eu.domibus.core.alerts.service;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JMSConstants;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.persist.AbstractEventProperty;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.service.DefaultMailModel;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.time.ZoneId;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_RETRY_TIME;
import static eu.domibus.core.alerts.model.common.AlertStatus.*;
import static eu.domibus.core.alerts.service.AlertConfigurationServiceImpl.DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertServiceImpl.class);

    static final String ALERT_LEVEL = "ALERT_LEVEL";
    static final String ALERT_SUBJECT = "ALERT_SUBJECT";
    static final String ALERT_ACTIVE = "ALERT_ACTIVE";
    static final String ALERT_NAME = "ALERT_NAME";

    static final String REPORTING_TIME = "REPORTING_TIME";

    /**
     * server name on which Domibus is running
     */
    static final String SERVER_NAME = "SERVER_NAME";

    public static final String DESCRIPTION = "DESCRIPTION";

    static final String ALERT_SELECTOR = "alert";
    public static final String ALERT_DESCRIPTION = "ALERT_DESCRIPTION";
    public static final String NOT_AVAILABLE = "na";

    private final EventDao eventDao;

    private final AlertDao alertDao;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomibusCoreMapper coreMapper;

    private final JMSManager jmsManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final Queue alertMessageQueue;

    private final AlertConfigurationService alertConfigurationService;

    private final ServerInfoService serverInfoService;

    private final CommonConfigurationManager alertConfigurationManager;

    public AlertServiceImpl(EventDao eventDao,
                            AlertDao alertDao,
                            DomibusPropertyProvider domibusPropertyProvider,
                            DomibusCoreMapper coreMapper, JMSManager jmsManager,
                            @Qualifier(JMSConstants.ALERT_MESSAGE_QUEUE) Queue alertMessageQueue,
                            AlertConfigurationService alertConfigurationService,
                            ServerInfoService serverInfoService,
                            CommonConfigurationManager alertConfigurationManager) {
        this.eventDao = eventDao;
        this.alertDao = alertDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.coreMapper = coreMapper;
        this.jmsManager = jmsManager;
        this.alertMessageQueue = alertMessageQueue;
        this.alertConfigurationService = alertConfigurationService;
        this.serverInfoService = serverInfoService;
        this.alertConfigurationManager = alertConfigurationManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public eu.domibus.core.alerts.model.service.Alert createAlertOnEvent(eu.domibus.core.alerts.model.service.Event event) {
        AlertModuleConfiguration moduleConfiguration = alertConfigurationService.getModuleConfiguration(AlertType.getByEventType(event.getType()));
        return createAlert(event, moduleConfiguration.getAlertLevel(event), moduleConfiguration.isActive());
    }

    private eu.domibus.core.alerts.model.service.Alert createAlert(eu.domibus.core.alerts.model.service.Event event, AlertLevel alertLevel, boolean active) {
        final Event eventEntity = readEvent(event);
        AlertType alertType = AlertType.getByEventType(event.getType());
        if (!active) {
            LOG.debug("Alerts of type [{}] are currently disabled", alertType);
            return null;
        }
        if (alertLevel == null) {
            LOG.debug("Alert of type [{}] currently disabled for this event: [{}]", alertType, event);
            return null;
        }

        Alert alert = new Alert();
        alert.addEvent(eventEntity);
        alert.setAlertType(alertType);
        alert.setAttempts(0);
        alert.setMaxAttempts(domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS));
        alert.setAlertStatus(SEND_ENQUEUED);
        alert.setCreationTime(new Date());
        alert.setAlertLevel(alertLevel);
        alertDao.create(alert);
        LOG.info("New alert saved: [{}]", alert);
        return coreMapper.alertPersistToAlertService(alert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void createAndEnqueueAlertOnPluginEvent(eu.domibus.core.alerts.model.service.Event event) {

        AlertLevel alertLevel = event.findOptionalProperty(ALERT_LEVEL)
                .map(AlertLevel::valueOf)
                .orElse(null);
        String alertName = event.findOptionalProperty(ALERT_NAME)
                .orElse(null);
        boolean active = BooleanUtils.toBoolean(event.findOptionalProperty(ALERT_ACTIVE).orElse(null));

        eu.domibus.core.alerts.model.service.Alert alert = createAlert(event, alertLevel, active);
        LOG.debug("Alert [{}] created and queued", alertName);
        enqueueAlert(alert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueueAlert(eu.domibus.core.alerts.model.service.Alert alert) {
        if (alert == null) {
            LOG.debug("Alert not enqueued");
            return;
        }
        jmsManager.convertAndSendToQueue(alert, alertMessageQueue, ALERT_SELECTOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MailModel<Map<String, String>> getMailModelForAlert(eu.domibus.core.alerts.model.service.Alert alert) {
        final Alert alertEntity = readAlert(alert);
        alertEntity.setReportingTime(new Date());
        Map<String, String> mailModel = new HashMap<>();
        final Event next = alertEntity.getEvents().iterator().next();
        next.getProperties().forEach((key, value) -> mailModel.put(key, StringEscapeUtils.escapeHtml4(value.getValue().toString())));
        mailModel.put(ALERT_LEVEL, alertEntity.getAlertLevel().name());
        mailModel.put(REPORTING_TIME, DateUtil.DEFAULT_FORMATTER.withZone(ZoneId.systemDefault()).format(alertEntity.getReportingTime().toInstant()));
        mailModel.put(DESCRIPTION, getDescription(alertEntity, next));
        mailModel.put(SERVER_NAME, serverInfoService.getServerName());

        if (LOG.isDebugEnabled()) {
            mailModel.forEach((key, value) -> LOG.debug("Mail template key[{}] value[{}]", key, value));
        }
        final AlertType alertType = alertEntity.getAlertType();
        String subject = getSubject(alertType, next);
        final String template = alertType.getTemplate();
        return new DefaultMailModel<>(mailModel, template, subject);
    }

    protected String getDescription(Alert alertEntity, Event next) {
        String title = "[" + alertEntity.getAlertType().getTitle() + "] ";
        AbstractEventProperty<?> description = next.getProperties().get(ALERT_DESCRIPTION);
        if (description != null) {
            title += description.getValue().toString();
        }
        return title;
    }

    protected String getSubject(AlertType alertType, Event next) {
        String subject = null;
        AbstractEventProperty<?> alertSubject = next.getProperties().get(ALERT_SUBJECT);
        if (alertSubject != null) {
            subject = alertSubject.getValue().toString();
        }
        if (StringUtils.isBlank(subject)) {
            subject = alertConfigurationService.getMailSubject(alertType);
        }

        //always set at super level
        final String serverName = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT);
        subject += "[" + serverName + "]";
        return subject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAlertStatus(eu.domibus.core.alerts.model.service.Alert alert) {
        final Alert alertEntity = readAlert(alert);
        if (alertEntity == null) {
            LOG.error("Alert[{}]: not found", alert.getEntityId());
            return;
        }
        alertEntity.setAlertStatus(alert.getAlertStatus());
        alertEntity.setNextAttempt(null);
        if (SUCCESS == alertEntity.getAlertStatus()) {
            alertEntity.setReportingTime(new Date());
            alertEntity.setAttempts(alertEntity.getAttempts() + 1);
            LOG.debug("Alert[{}]: send successfully", alert.getEntityId());
            return;
        }
        final Integer attempts = alertEntity.getAttempts() + 1;
        final Integer maxAttempts = alertEntity.getMaxAttempts();
        LOG.debug("Alert[{}]: send unsuccessfully", alert.getEntityId());
        if (attempts < maxAttempts) {
            LOG.debug("Alert[{}]: send attempts[{}], max attempts[{}]", alert.getEntityId(), attempts, maxAttempts);
            final Integer minutesBetweenAttempt = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_RETRY_TIME);
            final Date nextAttempt = org.joda.time.LocalDateTime.now().plusMinutes(minutesBetweenAttempt).toDate();
            alertEntity.setNextAttempt(nextAttempt);
            alertEntity.setAttempts(attempts);
            alertEntity.setAlertStatus(RETRY);
        }

        if (FAILED == alertEntity.getAlertStatus()) {
            alertEntity.setReportingTimeFailure(org.joda.time.LocalDateTime.now().toDate());
            alertEntity.setAttempts(alertEntity.getMaxAttempts());
        }
        LOG.debug("Alert[{}]: change status to:[{}]", alert.getEntityId(), alertEntity.getAlertStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retrieveAndResendFailedAlerts() {
        final List<Alert> retryAlerts = alertDao.findRetryAlerts();
        retryAlerts.forEach(this::convertAndEnqueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<eu.domibus.core.alerts.model.service.Alert> findAlerts(AlertCriteria alertCriteria) {
        final List<Alert> alerts = alertDao.filterAlerts(alertCriteria);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Find alerts:");
            alerts.forEach(alert -> {
                LOG.debug("Alert[{}]", alert);
                alert.getEvents().forEach(event -> {
                    LOG.debug("Event[{}]", event);
                    event.getProperties().
                            forEach((key, value) -> LOG.debug("Event property:[{}]->[{}]", key, value));
                });
            });

        }
        return coreMapper.alertPersistListToAlertServiceList(alerts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long countAlerts(AlertCriteria alertCriteria) {
        return alertDao.countAlerts(alertCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void cleanAlerts() {
        final Integer alertLifeTimeInDays = alertConfigurationManager.getConfiguration().getAlertLifeTimeInDays();
        final Date alertLimitDate = org.joda.time.LocalDateTime.now().minusDays(alertLifeTimeInDays).withTime(0, 0, 0, 0).toDate();
        LOG.debug("Cleaning alerts with creation time < [{}]", alertLimitDate);
        final List<Alert> alerts = alertDao.retrieveAlertsWithCreationDateSmallerThen(alertLimitDate);
        alertDao.deleteAll(alerts);
        LOG.trace("[{}] old alerts deleted", alerts.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateAlertProcessed(List<eu.domibus.core.alerts.model.service.Alert> alerts) {
        alerts.forEach(alert -> {
            final long entityId = alert.getEntityId();
            final boolean processed = alert.isProcessed();
            LOG.debug("Update alert with id[{}] set processed to[{}]", entityId, processed);
            alertDao.updateAlertProcessed(entityId, processed);
        });

    }

    private void convertAndEnqueue(Alert alert) {
        LOG.debug("Preparing alert for retry [{}]", alert);
        final eu.domibus.core.alerts.model.service.Alert convert = coreMapper.alertPersistToAlertService(alert);
        enqueueAlert(convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAlerts(List<eu.domibus.core.alerts.model.service.Alert> alerts) {
        LOG.info("Deleting alerts: {}", alerts);

        alerts.stream()
                .map(this::readAlert)
                .filter(Objects::nonNull)
                .forEach(this::deleteAlert);
    }

    private Event readEvent(eu.domibus.core.alerts.model.service.Event event) {
        return eventDao.read(event.getEntityId());
    }

    private Alert readAlert(eu.domibus.core.alerts.model.service.Alert alert) {
        return alertDao.read(alert.getEntityId());
    }

    protected void deleteAlert(Alert alert) {
        LOG.debug("Deleting alert by first detaching it from its events: [{}]", alert);
        alert.getEvents().forEach(event -> event.removeAlert(alert));
        alertDao.delete(alert);
    }
}
