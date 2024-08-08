package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.listener.EArchiveListener;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.AbstractIT;
import org.apache.activemq.command.ActiveMQMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class EArchivingStuckBatchesServiceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingStuckBatchesServiceIT.class);

    @Autowired
    EArchivingStuckBatchesService eArchivingStuckBatchesService;

    @Autowired
    EArchiveBatchDispatcherService eArchiveBatchDispatcherService;

    @Autowired
    EArchiveListener eArchiveListener;

    @Autowired
    EArchiveBatchDao eArchiveBatchDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    protected EArchiveFileStorageProvider storageProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private File temp;

    private EArchiveBatchEntity eArchiveBatchStarted;
    private EArchiveBatchEntity eArchiveBatchQueued;

    @Before
    public void setup() throws IOException {

        temp = Files.createTempDirectory(Paths.get("target"), "tmpDirPrefix").toFile();
        LOG.info("temp folder created: [{}]", temp.getAbsolutePath());

        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION, temp.getAbsolutePath());
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DOMIBUS_EARCHIVE_ACTIVE, "true");

        storageProvider.initialize();

        eArchiveBatchStarted = createEarchiveBatch(EArchiveBatchStatus.STARTED);
        eArchiveBatchQueued = createEarchiveBatch(EArchiveBatchStatus.QUEUED);
    }

    private EArchiveBatchEntity createEarchiveBatch(EArchiveBatchStatus eArchiveBatchStatus) {
        EArchiveBatchEntity eArchiveBatch = new EArchiveBatchEntity();
        eArchiveBatch.setBatchId(UUID.randomUUID().toString());
        eArchiveBatch.setRequestType(EArchiveRequestType.CONTINUOUS);
        eArchiveBatch.setCreationTime(dateUtil.getUtcDate());
        eArchiveBatch.setDateRequested(dateUtil.getDateMinutesAgo(1000));
        eArchiveBatch.setEArchiveBatchStatus(eArchiveBatchStatus);
        eArchiveBatch.setCreatedBy("test");
        eArchiveBatchDao.create(eArchiveBatch);
        return eArchiveBatch;
    }

    @Test
    @Transactional
    public void create() {
        ReflectionTestUtils.setField(eArchiveBatchDispatcherService, "jmsManager", new JMSManagerImpl() {
            public void sendMessageToQueue(JmsMessage message, Queue destination) {
                ActiveMQMessage mqMessage = new ActiveMQMessage();
                for (Map.Entry<String, String> stringStringEntry : message.getProperties().entrySet()) {
                    try {
                        mqMessage.setStringProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
                mqMessage.setJMSType(EArchiveBatchStatus.EXPORTED.name());
                eArchiveListener.onMessage(mqMessage);
            }
        });
        eArchivingStuckBatchesService.reExportStuckBatches();


        EArchiveBatchEntity queued = eArchiveBatchDao.findByReference(eArchiveBatchQueued.getEntityId());
        EArchiveBatchEntity started = eArchiveBatchDao.findByReference(eArchiveBatchStarted.getEntityId());

        Assert.assertEquals(EArchiveBatchStatus.FAILED, queued.getEArchiveBatchStatus());
        Assert.assertEquals(EArchiveBatchStatus.FAILED, started.getEArchiveBatchStatus());

        Assert.assertEquals(EArchiveBatchStatus.EXPORTED, getEArchiveBatchEntityWithOrigin(queued).getEArchiveBatchStatus());
        Assert.assertEquals(EArchiveBatchStatus.EXPORTED, getEArchiveBatchEntityWithOrigin(started).getEArchiveBatchStatus());
    }

    private EArchiveBatchEntity getEArchiveBatchEntityWithOrigin(EArchiveBatchEntity queued) {
        TypedQuery<EArchiveBatchEntity> query = em.createQuery("SELECT eaum FROM EArchiveBatchEntity eaum where originalBatchId = :BATCH_ID", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ID", queued.getBatchId());
        return query.getSingleResult();
    }
}
