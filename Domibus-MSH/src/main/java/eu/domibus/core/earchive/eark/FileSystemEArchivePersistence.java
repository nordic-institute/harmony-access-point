package eu.domibus.core.earchive.eark;

import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
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
    public static final String FOLDER_REPRESENTATION_1_NEW = IPConstants.REPRESENTATIONS_FOLDER + "representation1";
    public static final String BATCH_JSON_PATH = FOLDER_REPRESENTATION_1 + IPConstants.DATA_FOLDER + BATCH_JSON;

    protected final EArchiveFileStorageProvider storageProvider;

    protected final DomibusVersionService domibusVersionService;

    private final EArchivingFileService eArchivingFileService;

    private final EARKSIPFileService eArkSipBuilderService;


    public FileSystemEArchivePersistence(EArchiveFileStorageProvider storageProvider,
                                         DomibusVersionService domibusVersionService,
                                         EArchivingFileService eArchivingFileService,
                                         EARKSIPFileService earksipFileService) {
        this.storageProvider = storageProvider;
        this.domibusVersionService = domibusVersionService;
        this.eArchivingFileService = eArchivingFileService;
        this.eArkSipBuilderService = earksipFileService;
    }

    @Override
    public DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds) {
        String batchId = batchEArchiveDTO.getBatchId();
        LOG.info("Create earchive structure for batchId [{}] with [{}] messages", batchId, userMessageEntityIds.size());
        try {
            Path batchDirectory = Paths.get(storageProvider.getCurrentStorage().getStorageDirectory().getAbsolutePath(), batchId);
            if (Files.exists(batchDirectory)) {
                BasicFileAttributes attr = Files.readAttributes(batchDirectory, BasicFileAttributes.class);
                LOG.warn("File already exists: creationTime: [{}] | lastAccessTime: [{}] | lastModifiedTime: [{}]",
                        attr.creationTime(),
                        attr.lastAccessTime(),
                        attr.lastModifiedTime());
            }
            Files.createDirectory(batchDirectory);

            MetsWrapper mainMETSWrapper = eArkSipBuilderService.getMetsWrapper(
                    domibusVersionService.getArtifactName(),
                    domibusVersionService.getDisplayVersion(),
                    batchId);

            addRepresentation1(userMessageEntityIds, batchDirectory, mainMETSWrapper);

            Path path = eArkSipBuilderService.addMetsFileToFolder(batchDirectory, mainMETSWrapper);
            String checksum = eArkSipBuilderService.getChecksum(path);
            batchEArchiveDTO.setManifestChecksum(checksum);
            createBatchJson(batchEArchiveDTO, batchDirectory);

            return new DomibusEARKSIPResult(batchDirectory, checksum);
        } catch (IPException | IOException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveDTO + "]", e);
        }
    }

    private void createBatchJson(BatchEArchiveDTO batchEArchiveDTO, Path batchDirectory) {
        try (InputStream inputStream = eArchivingFileService.getBatchFileJson(batchEArchiveDTO)) {
            Path path = Paths.get(batchDirectory.toFile().getAbsolutePath(), BATCH_JSON_PATH);
            Files.createFile(path);
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

    private void addUserMessage(EArchiveBatchUserMessage messageId, Path batchDirectory, MetsWrapper mainMETSWrapper) {
        Map<String, InputStream> archivingFile = eArchivingFileService.getArchivingFiles(messageId.getUserMessageEntityId());

        for (Map.Entry<String, InputStream> file : archivingFile.entrySet()) {
            LOG.trace("Process file [{}]", file.getKey());
            String relativePathToMessageFolder = IPConstants.DATA_FOLDER + messageId.getMessageId() + IPConstants.ZIP_PATH_SEPARATOR + file.getKey();

            Path dir = Paths.get(batchDirectory.toFile().getAbsolutePath(), "representations", "representation1", "data", messageId.getMessageId());
            Path path = Paths.get(dir.toFile().getAbsolutePath(), file.getKey());
            try {
                if (Files.exists(dir)) {
                    BasicFileAttributes attr = Files.readAttributes(dir, BasicFileAttributes.class);
                    LOG.warn("File already exists: creationTime: [{}] | lastAccessTime: [{}] | lastModifiedTime: [{}]",
                            attr.creationTime(),
                            attr.lastAccessTime(),
                            attr.lastModifiedTime());
                }
                Files.createDirectories(dir);
                Files.createFile(path);
            } catch (IOException e) {
                throw new DomibusEArchiveException("Could not access to the folder [" + batchDirectory + "] and file [" + relativePathToMessageFolder + "]", e);
            }
            try (InputStream inputStream = file.getValue()) {

                eArkSipBuilderService.createDataFile(path, inputStream);

            } catch (IOException e) {
                throw new DomibusEArchiveException("Could not access to the folder [" + batchDirectory + "] and file [" + relativePathToMessageFolder + "]", e);
            }
            eArkSipBuilderService.addDataFileInfoToMETS(mainMETSWrapper, relativePathToMessageFolder, path);

        }
    }

}
