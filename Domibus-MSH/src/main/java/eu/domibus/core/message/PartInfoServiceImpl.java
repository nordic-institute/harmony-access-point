package eu.domibus.core.message;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.encryption.DecryptDataSource;
import eu.domibus.api.message.compression.DecompressionDataSource;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.payload.persistence.PayloadPersistenceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.crypto.Cipher;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service //("PartInfoService")
public class PartInfoServiceImpl implements PartInfoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfoServiceImpl.class);

    public static final String PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD = DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD;

    protected static final Long BYTES_IN_MB = 1048576L;

    @Autowired
    private PartInfoDao partInfoDao;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PayloadPersistenceHelper payloadPersistenceHelper;

    @Override
    public void create(PartInfo partInfo, UserMessage userMessage) {
        partInfo.setUserMessage(userMessage);
        partInfoDao.create(partInfo);
    }

    @Override
    public List<PartInfo> findPartInfo(UserMessage userMessage) {
        return partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
    }

    @Override
    public PartInfo findPartInfo(String messageId, String cid) {
        return partInfoDao.findPartInfoByUserMessageIdAndCid(messageId, getCid(cid));
    }

    @Override
    public PartInfo findPartInfo(Long messageEntityId, String cid) {
        return partInfoDao.findPartInfoByUserMessageEntityIdAndCid(messageEntityId, getCid(cid));
    }

    protected String getCid(String cid) {
        if (StringUtils.startsWith(cid, "cid:")) {
            return cid;
        }
        return "cid:" + cid;
    }

    @Override
    public List<PartInfo> findPartInfo(long entityId) {
        return partInfoDao.findPartInfoByUserMessageEntityId(entityId);
    }

    @Override
    public Long findPartInfoTotalLength(long entityId) {
        List<Long> list = partInfoDao.findPartInfoLengthByUserMessageEntityId(entityId);
        return list.stream().mapToLong(Long::longValue).sum();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void clearPayloadData(long entityId) {
        LOG.debug("Start clearing payloadData");

        List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(entityId);

        if (CollectionUtils.isEmpty(partInfos)) {
            LOG.debug("No payloads to clear");
            return;
        }
        clearDatabasePayloads(partInfos);
        clearFileSystemPayloads(partInfos);

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_DATA_CLEARED);
    }

    @Override
    public void clearFileSystemPayloads(final List<PartInfo> partInfos) {
        List<PartInfo> fileSystemPayloads = getFileSystemPayloads(partInfos);
        if (CollectionUtils.isEmpty(fileSystemPayloads)) {
            LOG.debug("No file system payloads to clear");
            return;
        }

        for (PartInfo result : fileSystemPayloads) {
            deletePayloadFile(result.getFileName());
        }
    }

    protected List<PartInfo> getFileSystemPayloads(final List<PartInfo> partInfos) {
        Predicate<PartInfo> filenamePresentPredicate = getFilenameEmptyPredicate().negate();
        return getPayloads(partInfos, filenamePresentPredicate);
    }

    protected List<PartInfo> getPayloads(final List<PartInfo> partInfos, Predicate<PartInfo> partInfoPredicate) {
        return partInfos.stream().filter(partInfoPredicate).collect(Collectors.toList());
    }

    protected Predicate<PartInfo> getFilenameEmptyPredicate() {
        return partInfo -> StringUtils.isBlank(partInfo.getFileName());
    }

    protected void deletePayloadFile(String filename) {
        if (StringUtils.isAllBlank(filename)) {
            LOG.warn("Empty filename used to delete payload on filesystem!");
            return;
        }

        try {
            Path path = Paths.get(filename);
            if (path == null) {
                LOG.warn("Trying to delete an empty path, filename [{}]", filename);
                return;
            }
            Files.delete(path);
        } catch (IOException e) {
            LOG.debug("Problem deleting payload data files", e);
        }
    }

    @Override
    public void deletePayloadFiles(List<String> filenames) {
        if (CollectionUtils.isEmpty(filenames)) {
            LOG.debug("No payload data file to delete from the filesystem");
            return;
        }

        LOG.debug("There are [{}] payloads on filesystem to delete: [{}] ", filenames.size(), filenames);
        for (String filename : filenames) {
            LOG.debug("Deleting payload data file: [{}]", filename);
            deletePayloadFile(filename);
        }
    }

    protected void clearDatabasePayloads(final List<PartInfo> partInfos) {
        List<PartInfo> databasePayloads = getDatabasePayloads(partInfos);
        if (CollectionUtils.isEmpty(databasePayloads)) {
            LOG.debug("No database payloads to clear");
            return;
        }

        partInfoDao.clearDatabasePayloads(databasePayloads);
    }

    protected List<PartInfo> getDatabasePayloads(final List<PartInfo> partInfos) {
        Predicate<PartInfo> filenameEmptyPredicate = getFilenameEmptyPredicate();
        return getPayloads(partInfos, filenameEmptyPredicate);
    }

    @Override
    public List<String> findFileSystemPayloadFilenames(List<String> userMessageEntityIds) {
        return partInfoDao.findFileSystemPayloadFilenames(userMessageEntityIds);
    }

    @Override
    public boolean scheduleSourceMessagePayloads(List<PartInfo> partInfos) {
        if (CollectionUtils.isEmpty(partInfos)) {
            LOG.debug("SourceMessages does not have any payloads");
            return false;
        }

        long totalPayloadLength = 0;
        for (PartInfo partInfo : partInfos) {
            totalPayloadLength += partInfo.getLength();
        }
        LOG.debug("SourceMessage payloads totalPayloadLength(bytes) [{}]", totalPayloadLength);

        final Long payloadsScheduleThresholdMB = domibusPropertyProvider.getLongProperty(PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
        LOG.debug("Using configured payloadsScheduleThresholdMB [{}]", payloadsScheduleThresholdMB);

        final long payloadsScheduleThresholdBytes = payloadsScheduleThresholdMB * BYTES_IN_MB;
        if (totalPayloadLength > payloadsScheduleThresholdBytes) {
            LOG.debug("The SourceMessage payloads will be scheduled for saving");
            return true;
        }
        return false;

    }

    /**
     * WARNING: for message sent, the compress flag is NOT set and the {@link #getPayloadDatahandler} will provide the zipped data
     */
    @Override
    public void loadBinaryData(PartInfo partInfo) {
        String fileName = partInfo.getFileName();

        if (fileName != null) { /* Create payload data handler from File */
            LOG.debug("LoadBinary from file: [{}]", fileName);
            DataSource fsDataSource = new AutoCloseFileDataSource(fileName);
            createPayloadDataHandler(partInfo, fsDataSource);
            return;
        }
        /* Create payload data handler from binaryData (byte[]) */
        byte[] binaryData = partInfo.getBinaryData();
        if (binaryData == null) {
            LOG.debug("Payload is empty!");
            partInfo.setPayloadDatahandler(null);
        } else {
            DataSource dataSource = new ByteArrayDataSource(binaryData, partInfo.getMime());
            createPayloadDataHandler(partInfo, dataSource);
        }
    }

    private void createPayloadDataHandler(PartInfo partInfo, DataSource fsDataSource) {
        String href = partInfo.getHref();
        if (partInfo.isEncrypted()) {
            LOG.debug("Using DecryptDataSource for payload [{}]", href);
            final Cipher decryptCipher = getDecryptCipher(partInfo);
            fsDataSource = new DecryptDataSource(fsDataSource, decryptCipher);
        }

        if (BooleanUtils.isTrue(partInfo.getCompressed())) {
            LOG.debug("Setting the decompressing handler on the the payload [{}]", href);
            fsDataSource = new DecompressionDataSource(fsDataSource, partInfo.getMime());
        }

        partInfo.setPayloadDatahandler(new DataHandler(fsDataSource));
    }

    private Cipher getDecryptCipher(PartInfo partInfo) {
        LOG.debug("Getting decrypt cipher for payload [{}]", partInfo.getHref());
        final PayloadEncryptionService encryptionService = SpringContextProvider.getApplicationContext().getBean("EncryptionServiceImpl", PayloadEncryptionService.class);
        return encryptionService.getDecryptCipherForPayload();
    }

    public void validatePayloadSizeBeforeSchedulingSave(LegConfiguration legConfiguration, List<PartInfo> partInfos) {
        for (PartInfo partInfo : partInfos) {
            payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength(), true);
        }
    }

    /**
     * @param userMessage the UserMessage received
     * @throws EbMS3Exception if an attachment with an invalid charset is received
     *                        (not {@link Property#CHARSET} or not matching {@link Property#CHARSET_PATTERN})
     */
    public void checkPartInfoCharset(final UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
        LOG.debug("Checking charset for attachments");
        if (CollectionUtils.isEmpty(partInfoList)) {
            LOG.debug("No partInfo found");
            return;
        }

        for (final PartInfo partInfo : partInfoList) {
            if (CollectionUtils.isEmpty(partInfo.getPartProperties())) {
                continue;
            }
            for (final Property property : partInfo.getPartProperties()) {
                if (Property.CHARSET.equalsIgnoreCase(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, property.getValue(), userMessage.getMessageId());
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                            .message(property.getValue() + " is not a valid Charset")
                            .refToMessageId(userMessage.getMessageId())
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                }
            }
        }
    }

}
