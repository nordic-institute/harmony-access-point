package eu.domibus.core.earchive.eark;

import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.earchive.DomibusEArchiveExportException;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.alerts.EArchivingEventService;
import eu.domibus.core.earchive.storage.EArchiveFileStorage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class FileSystemEArchivePersistence implements EArchivePersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistence.class);
    public static final String BATCH_JSON = "batch.json";
    public static final String FOLDER_REPRESENTATION_1 = IPConstants.REPRESENTATIONS_FOLDER + "representation1" + IPConstants.ZIP_PATH_SEPARATOR;
    public static final String BATCH_JSON_PATH = FOLDER_REPRESENTATION_1 + IPConstants.DATA_FOLDER + BATCH_JSON;

    protected final EArchiveFileStorageProvider storageProvider;

    protected final DomibusVersionService domibusVersionService;

    private final EArchivingFileService eArchivingFileService;

    private final EARKSIPFileService eArkSipBuilderService;

    private final EArchivingEventService eArchivingEventService;
    private final FileServiceUtil fileServiceUtil;


    public FileSystemEArchivePersistence(EArchiveFileStorageProvider storageProvider,
                                         DomibusVersionService domibusVersionService,
                                         EArchivingFileService eArchivingFileService,
                                         EARKSIPFileService earksipFileService,
                                         EArchivingEventService eArchivingEventService,
                                         FileServiceUtil fileServiceUtil) {
        this.storageProvider = storageProvider;
        this.domibusVersionService = domibusVersionService;
        this.eArchivingFileService = eArchivingFileService;
        this.eArkSipBuilderService = earksipFileService;
        this.eArchivingEventService = eArchivingEventService;
        this.fileServiceUtil = fileServiceUtil;
    }

    @Override
    @Timer(clazz = FileSystemEArchivePersistence.class, value = "earchive2_createEArkSipStructure")
    @Counter(clazz = FileSystemEArchivePersistence.class, value = "earchive2_createEArkSipStructure")
    public DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds, Date messageStartDate, Date messageEndDate) {
        String batchId = batchEArchiveDTO.getBatchId();
        LOG.info("Create eArchive structure for batchId [{}] with [{}] messages", batchId, userMessageEntityIds.size());
        try {
            EArchiveFileStorage currentStorage = storageProvider.getCurrentStorage();
            File storageDirectory = currentStorage.getStorageDirectory();
            String absolutePath = storageDirectory.getAbsolutePath();
            Path batchDirectory = Paths.get(absolutePath, batchId);
            FileUtils.forceMkdir(batchDirectory.toFile());
            MetsWrapper mainMETSWrapper = eArkSipBuilderService.getMetsWrapper(
                    domibusVersionService.getArtifactName(),
                    domibusVersionService.getDisplayVersion(),
                    batchId);

            addRepresentation1(userMessageEntityIds, batchDirectory, mainMETSWrapper);

            Path path = eArkSipBuilderService.addMetsFileToFolder(batchDirectory, mainMETSWrapper);
            String checksum = eArkSipBuilderService.getChecksum(path);
            batchEArchiveDTO.setManifestChecksum(checksum);

            if (messageStartDate != null && messageEndDate != null) {
                batchEArchiveDTO.setMessageStartDate(DateTimeFormatter.ISO_DATE_TIME.format(messageStartDate.toInstant().atZone(ZoneOffset.UTC)));
                batchEArchiveDTO.setMessageEndDate(DateTimeFormatter.ISO_DATE_TIME.format(messageEndDate.toInstant().atZone(ZoneOffset.UTC)));
            }
            createBatchJson(batchEArchiveDTO, batchDirectory);

            return new DomibusEARKSIPResult(batchDirectory, checksum);
        } catch (DomibusEArchiveExportException ex) {
            if (EArchiveRequestType.SANITIZER.name().equals(batchEArchiveDTO.getRequestType())) {
                eArchivingEventService.sendEventExportFailed(batchEArchiveDTO.getBatchId(), ex.getEntityId(), ex.getMessage());
            }
            throw ex;
        } catch (IPException | IOException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveDTO + "]", e);
        }
    }


    private void createBatchJson(BatchEArchiveDTO batchEArchiveDTO, Path batchDirectory) {
        try (InputStream inputStream = eArchivingFileService.getBatchFileJson(batchEArchiveDTO)) {
            Path path = Paths.get(batchDirectory.toFile().getAbsolutePath(), BATCH_JSON_PATH);
            eArkSipBuilderService.createDataFile(path, inputStream);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not write the file " + BATCH_JSON);
        }

    }

    protected void addRepresentation1(List<EArchiveBatchUserMessage> userMessageEntityIds, Path batchDirectory, MetsWrapper mainMETSWrapper) {
        eArkSipBuilderService.addBatchJsonToMETS(mainMETSWrapper, BATCH_JSON_PATH);
        for (EArchiveBatchUserMessage eArchiveBatchUserMessage : userMessageEntityIds) {
            LOG.debug("Add messageId [{}]", eArchiveBatchUserMessage.getMessageId());
            addUserMessage(eArchiveBatchUserMessage, batchDirectory, mainMETSWrapper);
        }
    }

    private void addUserMessage(EArchiveBatchUserMessage eArchiveBatchUserMessage, Path batchDirectory, MetsWrapper mainMETSWrapper) {
        Map<String, ArchivingFileDTO> archivingFile = eArchivingFileService.getArchivingFiles(eArchiveBatchUserMessage.getUserMessageEntityId());

        for (Map.Entry<String, ArchivingFileDTO> file : archivingFile.entrySet()) {
            LOG.trace("Process file [{}]", file.getKey());
            String messageFolder = fileServiceUtil.URLEncode(eArchiveBatchUserMessage.getMessageId());

            String relativePathToMessageFolder = IPConstants.DATA_FOLDER + messageFolder + IPConstants.ZIP_PATH_SEPARATOR + file.getKey();

            Path dir = Paths.get(batchDirectory.toFile().getAbsolutePath(), "representations", "representation1", "data", messageFolder);
            Path path = Paths.get(dir.toFile().getAbsolutePath(), file.getKey());

            ArchivingFileDTO archivingFileDTO = file.getValue();
            archivingFileDTO.setPath(path);
            try (InputStream inputStream = archivingFileDTO.getInputStream()) {
                eArkSipBuilderService.createDataFile(path, inputStream);
            } catch (IOException e) {
                throw new DomibusEArchiveException("Could not createDataFile on dir [" + dir + "] and file [" + archivingFileDTO + "]", e);
            }
            eArkSipBuilderService.addDataFileInfoToMETS(mainMETSWrapper, relativePathToMessageFolder, archivingFileDTO);
        }
    }
}
