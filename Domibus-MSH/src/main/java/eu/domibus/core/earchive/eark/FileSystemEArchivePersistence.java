package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

        try (FileObject batchDirectory = getBatchDirectory(batchId)) {
            batchDirectory.createFolder();

            MetsWrapper mainMETSWrapper = eArkSipBuilderService.getMetsWrapper(
                    domibusVersionService.getArtifactName(),
                    domibusVersionService.getDisplayVersion(),
                    batchId);

            addRepresentation1(userMessageEntityIds, batchDirectory, mainMETSWrapper);

            Path path = eArkSipBuilderService.addMetsFileToFolder(batchDirectory, mainMETSWrapper);
            String checksum = eArkSipBuilderService.getChecksum(path);
            batchEArchiveDTO.setManifestChecksum(checksum);
            createBatchJson(batchEArchiveDTO, batchDirectory);

            return new DomibusEARKSIPResult(batchDirectory.getPath(), checksum);
        } catch (IPException | FileSystemException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveDTO + "]", e);
        }
    }

    private FileObject getBatchDirectory(String batchId) throws FileSystemException {
        return VFS.getManager().resolveFile(storageProvider.getCurrentStorage().getStorageDirectory(), batchId);
    }


    private void createBatchJson(BatchEArchiveDTO batchEArchiveDTO, FileObject batchDirectory) {
        try (FileObject fileObject = batchDirectory.resolveFile(BATCH_JSON_PATH);
             InputStream inputStream = eArchivingFileService.getBatchFileJson(batchEArchiveDTO)) {
            eArkSipBuilderService.createDataFile(fileObject, inputStream);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not write the file " + BATCH_JSON);
        }
    }

    protected void addRepresentation1(List<EArchiveBatchUserMessage> userMessageEntityIds, FileObject batchDirectory, MetsWrapper mainMETSWrapper) {
        eArkSipBuilderService.addBatchJsonToMETS(mainMETSWrapper, BATCH_JSON_PATH);
        for (EArchiveBatchUserMessage messageId : userMessageEntityIds) {
            LOG.debug("Add messageId [{}]", messageId);
            addUserMessage(messageId, batchDirectory, mainMETSWrapper);
        }
    }

    private void addUserMessage(EArchiveBatchUserMessage messageId, FileObject batchDirectory, MetsWrapper mainMETSWrapper) {
        Map<String, InputStream> archivingFile = eArchivingFileService.getArchivingFiles(messageId.getUserMessageEntityId());

        for (Map.Entry<String, InputStream> file : archivingFile.entrySet()) {
            LOG.trace("Process file [{}]", file.getKey());
            String relativePathToMessageFolder = IPConstants.DATA_FOLDER + messageId.getMessageId() + IPConstants.ZIP_PATH_SEPARATOR + file.getKey();

            try (FileObject fileObject = batchDirectory.resolveFile(FileSystemEArchivePersistence.FOLDER_REPRESENTATION_1 + relativePathToMessageFolder);
                 InputStream inputStream = file.getValue()) {
                eArkSipBuilderService.createDataFile(fileObject, inputStream);
                eArkSipBuilderService.addDataFileInfoToMETS(mainMETSWrapper, relativePathToMessageFolder, fileObject);
            } catch (IOException e) {
                throw new DomibusEArchiveException("Could not access to the folder [" + batchDirectory + "] and file [" + relativePathToMessageFolder + "]");
            }
        }
    }

}
