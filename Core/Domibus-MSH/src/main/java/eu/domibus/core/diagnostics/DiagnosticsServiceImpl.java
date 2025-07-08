package eu.domibus.core.diagnostics;

import eu.domibus.api.diagnostics.DiagnosticsService;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.ext.domain.diagnostics.AlertsCountsDTO;
import eu.domibus.ext.domain.diagnostics.JmsQueuesInfoDTO;
import eu.domibus.ext.domain.diagnostics.MessagesByStatusCountsDTO;
import eu.domibus.ext.domain.diagnostics.VersionInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@Service
public class DiagnosticsServiceImpl implements DiagnosticsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsServiceImpl.class);

    private static final String VERSION_INFO = "versionInfo";
    private static final String JMS_QUEUES_INFO = "jmsQueuesInfo";
    private static final String ALERTS_COUNTS = "alertsCounts";
    private static final String MESSAGES_BY_STATUS_COUNTS = "messagesByStatusCounts";

    @Autowired
    private DomibusVersionService domibusVersionService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    private eu.domibus.core.alerts.service.AlertService alertService;

    @Autowired
    private eu.domibus.core.message.MessagesLogService messagesLogService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    public void logDiagnosticInfo() {
        List<String> diagnosticsList = domibusPropertyProvider.getCommaSeparatedPropertyValues(DomibusPropertyMetadataManagerSPI.DOMIBUS_DIAGNOSTICS_LIST);
        String domainName = domainContextProvider.getCurrentDomainSafely().getCode();

        if (diagnosticsList.isEmpty()) {
            LOG.debug("Diagnostics list is empty, skipping diagnostics for domain [{}]", domainName);
            return;
        }

        LOG.info("Running Domibus diagnostics for domain [{}]", domainName);

        if (diagnosticsList.contains(VERSION_INFO)) {
            VersionInfoDTO versionInfo = getVersionInfo();
            LOG.info(versionInfo.toString());
        }

        if (diagnosticsList.contains(JMS_QUEUES_INFO)) {
            if (domibusConfigurationService.isMultiTenantAware()) {
                LOG.info("Diagnostics for JMS queues are not supported in multi-tenant mode, skipping diagnostics for domain [{}]", domainName);
            } else {
                JmsQueuesInfoDTO jmsQueuesInfo = getJmsQueuesInfo();
                LOG.info(jmsQueuesInfo.toString());
            }
        }

        if (diagnosticsList.contains(ALERTS_COUNTS)) {
            AlertsCountsDTO alertsCounts = getAlertsCounts();
            LOG.info(alertsCounts.toString());
        }

        if (diagnosticsList.contains(MESSAGES_BY_STATUS_COUNTS)) {
            MessagesByStatusCountsDTO messagesByStatusCounts = getMessagesByStatusCounts();
            LOG.info(messagesByStatusCounts.toString());
        }

        LOG.info("Diagnostics completed for domain [{}]", domainName);
    }

    @Override
    public VersionInfoDTO getVersionInfo() {
        return new VersionInfoDTO(
                domibusVersionService.getArtifactName(),
                domibusVersionService.getArtifactVersion(),
                domibusVersionService.getBuiltTime(),
                domibusVersionService.getVersionNumber()
        );
    }

    @Override
    public JmsQueuesInfoDTO getJmsQueuesInfo() {
        JmsQueuesInfoDTO dto = new JmsQueuesInfoDTO();
        Map<String, JMSDestination> destinations = jmsManager.getDestinations();
        for (Map.Entry<String, JMSDestination> entry : destinations.entrySet()) {
            JMSDestination destination = entry.getValue();
            long numberOfMessages = entry.getValue().getNumberOfMessages();
            dto.addQueue(destination.getName(), numberOfMessages);
        }
        return dto;
    }

    // TODO IB this might not be efficient
    @Override
    public AlertsCountsDTO getAlertsCounts() {
        AlertsCountsDTO dto = new AlertsCountsDTO();

        // Count alerts by status
        eu.domibus.core.alerts.model.common.AlertCriteria criteria = new eu.domibus.core.alerts.model.common.AlertCriteria();

        // Count all alerts
        Long totalCount = alertService.countAlerts(criteria);
        dto.setTotalCount(totalCount);

        // Count alerts by status
        for (eu.domibus.core.alerts.model.common.AlertStatus status : eu.domibus.core.alerts.model.common.AlertStatus.values()) {
            criteria = new eu.domibus.core.alerts.model.common.AlertCriteria();
            criteria.setAlertStatus(status.name());
            Long count = alertService.countAlerts(criteria);
            dto.addStatusCount(status.name(), count);
        }

        // Count alerts by type
        for (eu.domibus.core.alerts.model.common.AlertType type : eu.domibus.core.alerts.model.common.AlertType.values()) {
            criteria = new eu.domibus.core.alerts.model.common.AlertCriteria();
            criteria.setAlertType(type.name());
            Long count = alertService.countAlerts(criteria);
            dto.addTypeCount(type.name(), count);
        }

        return dto;
    }

    // TODO IB this might not be efficient
    @Override
    public MessagesByStatusCountsDTO getMessagesByStatusCounts() {
        MessagesByStatusCountsDTO dto = new MessagesByStatusCountsDTO();

        // Count user messages by status
        for (eu.domibus.api.model.MessageStatus status : eu.domibus.api.model.MessageStatus.values()) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("messageStatus", status);
            long count = messagesLogService.countMessages(eu.domibus.api.model.MessageType.USER_MESSAGE, filters);
            dto.addUserMessageCount(status.name(), count);
        }

        return dto;
    }
}
