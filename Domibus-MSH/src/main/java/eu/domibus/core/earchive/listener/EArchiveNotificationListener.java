package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import gen.eu.domibus.archive.client.api.ArchiveWebhookApi;
import gen.eu.domibus.archive.client.invoker.ApiClient;
import gen.eu.domibus.archive.client.model.BatchNotification;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class EArchiveNotificationListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveNotificationListener.class);

    private final DatabaseUtil databaseUtil;

    private final EArchivingDefaultService eArchiveService;

    private final EArchiveBatchUtils eArchiveBatchUtils;

    private final JmsUtil jmsUtil;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private SimpleDateFormat dateParser = new SimpleDateFormat("yyMMddHH");

    public EArchiveNotificationListener(
            DatabaseUtil databaseUtil,
            EArchivingDefaultService eArchiveService,
            EArchiveBatchUtils eArchiveBatchUtils,
            JmsUtil jmsUtil,
            DomibusPropertyProvider domibusPropertyProvider) {
        this.databaseUtil = databaseUtil;
        this.eArchiveService = eArchiveService;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
        this.jmsUtil = jmsUtil;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public void onMessage(Message message) {
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        String batchId = jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);

        Long entityId = jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        EArchiveBatchStatus notificationType = EArchiveBatchStatus.valueOf(jmsUtil.getStringPropertySafely(message, MessageConstants.NOTIFICATION_TYPE));

        EArchiveBatchEntity eArchiveBatch = eArchiveService.getEArchiveBatch(entityId);

        if (notificationType == EArchiveBatchStatus.FAILED) {
            LOG.info("Notification to the eArchive client for batch FAILED [{}] ", eArchiveBatch);
            ArchiveWebhookApi earchivingClientApi = initializeEarchivingClientApi();
            earchivingClientApi.putStaleNotification(buildBatchNotification(eArchiveBatch), batchId);
            LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_NOTIFICATION_SENT, eArchiveBatch.getBatchId());
        }

        if (notificationType == EArchiveBatchStatus.EXPORTED) {
            LOG.info("Notification to the eArchive client for batch EXPORTED [{}] ", eArchiveBatch);
            ArchiveWebhookApi earchivingClientApi = initializeEarchivingClientApi();
            earchivingClientApi.putExportNotification(buildBatchNotification(eArchiveBatch), batchId);
            LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_NOTIFICATION_SENT, eArchiveBatch.getBatchId());
        }
    }

    protected ArchiveWebhookApi initializeEarchivingClientApi() {
        String restUrl = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_URL);
        if (StringUtils.isBlank(restUrl)) {
            throw new DomibusEArchiveException("eArchive client endpoint not configured");
        }

        LOG.debug("Initializing eArchive client api with endpoint [{}]...", restUrl);

        int timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_NOTIFICATION_TIMEOUT);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .build();
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(restUrl);

        ArchiveWebhookApi earchivingClientApi = new ArchiveWebhookApi();
        earchivingClientApi.setApiClient(apiClient);

        String username = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_USERNAME);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_NOTIFICATION_PASSWORD);
        if (StringUtils.isNotBlank(username)) {
            earchivingClientApi.getApiClient().setUsername(username);
            earchivingClientApi.getApiClient().setPassword(password);
        }

        return earchivingClientApi;
    }

    protected BatchNotification buildBatchNotification(EArchiveBatchEntity eArchiveBatch) {
        BatchNotification batchNotification = new BatchNotification();
        batchNotification.setBatchId(eArchiveBatch.getBatchId());
        batchNotification.setErrorCode(eArchiveBatch.getErrorCode());
        batchNotification.setErrorDescription(eArchiveBatch.getErrorMessage());
        batchNotification.setStatus(BatchNotification.StatusEnum.valueOf(eArchiveBatch.getEArchiveBatchStatus().name()));
        batchNotification.setRequestType(BatchNotification.RequestTypeEnum.valueOf(eArchiveBatch.getRequestType().name()));
        batchNotification.setTimestamp(OffsetDateTime.ofInstant(eArchiveBatch.getDateRequested().toInstant(), ZoneOffset.UTC));

        ListUserMessageDto messageListDto = eArchiveBatchUtils.getUserMessageDtoFromJson(eArchiveBatch);
        List<String> messageIds = messageListDto.getUserMessageDtos().stream()
                .map(um -> um.getMessageId()).collect(Collectors.toList());
        batchNotification.setMessages(messageIds);

        Long firstPkUserMessage = messageListDto.getUserMessageDtos().stream()
                .map(um -> um.getEntityId()).reduce(Long::min).orElse(null);

        Date messageStartDate = dateFromLongDate(eArchiveBatchUtils.extractDateFromPKUserMessageId(firstPkUserMessage));
        Date messageEndDate = dateFromLongDate(eArchiveBatchUtils.extractDateFromPKUserMessageId(eArchiveBatch.getLastPkUserMessage()));
        batchNotification.setMessageStartDate(OffsetDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC));
        batchNotification.setMessageEndDate(OffsetDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC));

        return batchNotification;
    }

    private Date dateFromLongDate(Long dateAsLong) {
        try {
            return dateParser.parse(dateAsLong.toString());
        } catch (ParseException ex) {
            throw new DomibusEArchiveException("Invalid date: " + dateAsLong, ex);
        }
    }

}
