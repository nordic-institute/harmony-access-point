package eu.domibus.core.earchive.eark;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.BatchEArchiveBasicDTO;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

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

    @Autowired
    private MetricRegistry metricRegistry;


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
    @Timer(clazz = FileSystemEArchivePersistence.class, value = "earchive2_createEArkSipStructure")
    @Counter(clazz = FileSystemEArchivePersistence.class, value = "earchive2_createEArkSipStructure")
    public DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds) {
        String batchId = batchEArchiveDTO.getBatchId();
        LOG.info("Create earchive structure for batchId [{}] with [{}] messages", batchId, userMessageEntityIds.size());
        try {
            com.codahale.metrics.Timer.Context methodTimer = metricRegistry.timer(name("createEArkSipStructure", "batchDirectory", "timer")).time();
            Path batchDirectory = Paths.get(storageProvider.getCurrentStorage().getStorageDirectory().getAbsolutePath(), batchId);
            methodTimer.stop();
            com.codahale.metrics.Timer.Context cleanTimer = metricRegistry.timer(name("createEArkSipStructure", "createParentDirectories", "timer")).time();
            FileUtils.createParentDirectories(batchDirectory.toFile());
            cleanTimer.stop();
            com.codahale.metrics.Timer.Context eArkSi = metricRegistry.timer(name("createEArkSipStructure", "getMetsWrapper", "timer")).time();
            MetsWrapper mainMETSWrapper = eArkSipBuilderService.getMetsWrapper(
                    domibusVersionService.getArtifactName(),
                    domibusVersionService.getDisplayVersion(),
                    batchId);
            eArkSi.stop();

            com.codahale.metrics.Timer.Context addRep = metricRegistry.timer(name("createEArkSipStructure", "addRepresentation1", "timer")).time();
            addRepresentation1(userMessageEntityIds, batchDirectory, mainMETSWrapper);
            addRep.stop();


            com.codahale.metrics.Timer.Context addMes = metricRegistry.timer(name("createEArkSipStructure", "addMetsFileToFolder", "timer")).time();
            Path path = eArkSipBuilderService.addMetsFileToFolder(batchDirectory, mainMETSWrapper);
            addMes.stop();
            com.codahale.metrics.Timer.Context chkSum = metricRegistry.timer(name("createEArkSipStructure", "getChecksum", "timer")).time();
            String checksum = eArkSipBuilderService.getChecksum(path);
            chkSum.stop();
            batchEArchiveDTO.setManifestChecksum(checksum);
            com.codahale.metrics.Timer.Context crtBatch = metricRegistry.timer(name("createEArkSipStructure", "createBatchJson", "timer")).time();
            createBatchJson(batchEArchiveDTO, batchDirectory);
            crtBatch.stop();

            return new DomibusEARKSIPResult(batchDirectory, checksum);
        } catch (IPException | IOException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveDTO + "]", e);
        }
    }

    @Override
    public DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveBasicDTO batchEArchiveBasicDTO, List<EArchiveBatchUserMessage> userMessageEntityIds) {
        String batchId = batchEArchiveBasicDTO.getBatchId();
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
            batchEArchiveBasicDTO.setManifestChecksum(checksum);
            createBatchJson(batchEArchiveBasicDTO, batchDirectory);

            return new DomibusEARKSIPResult(batchDirectory.getPath(), checksum);
        } catch (IPException | FileSystemException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveBasicDTO + "]", e);
        }
    }

    private FileObject getBatchDirectory(String batchId) throws FileSystemException {
        return VFS.getManager().resolveFile(storageProvider.getCurrentStorage().getStorageDirectory(), batchId);
    }

    private void createBatchJson(BatchEArchiveDTO batchEArchiveDTO, Path batchDirectory) {
        try (InputStream inputStream = eArchivingFileService.getBatchFileJson(batchEArchiveDTO)) {
            Path path = Paths.get(batchDirectory.toFile().getAbsolutePath(), BATCH_JSON_PATH);
            eArkSipBuilderService.createDataFile(path, inputStream);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not write the file " + BATCH_JSON);
        }

    }

    private void createBatchJson(BatchEArchiveBasicDTO batchEArchiveBasicDTO, FileObject batchDirectory) {
        try (FileObject fileObject = batchDirectory.resolveFile(BATCH_JSON_PATH);
             InputStream inputStream = eArchivingFileService.getBatchFileJson(batchEArchiveBasicDTO)) {
            eArkSipBuilderService.createDataFile(fileObject, inputStream);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not write the file " + BATCH_JSON);
        }
    }

    protected void addRepresentation1(List<EArchiveBatchUserMessage> userMessageEntityIds, Path batchDirectory, MetsWrapper mainMETSWrapper) {
        com.codahale.metrics.Timer.Context addB = metricRegistry.timer(name("addRepresentation1", "addBatchJsonToMETS", "timer")).time();
        eArkSipBuilderService.addBatchJsonToMETS(mainMETSWrapper, BATCH_JSON_PATH);
        addB.stop();
        com.codahale.metrics.Timer.Context filLoc = metricRegistry.timer(name("addRepresentation1", "loop", "timer")).time();
        for (EArchiveBatchUserMessage eArchiveBatchUserMessage : userMessageEntityIds) {
            LOG.debug("Add messageId [{}]", eArchiveBatchUserMessage.getMessageId());
            com.codahale.metrics.Timer.Context addMess = metricRegistry.timer(name("addRepresentation1", "addUserMessage", "timer")).time();
            addUserMessage(eArchiveBatchUserMessage, batchDirectory, mainMETSWrapper);
            addMess.stop();
        }
        filLoc.stop();
    }

    private void addUserMessage(EArchiveBatchUserMessage messageId, Path batchDirectory, MetsWrapper mainMETSWrapper) {
        com.codahale.metrics.Timer.Context getArch = metricRegistry.timer(name("addUserMessage", "getArchivingFiles", "timer")).time();
        Map<String, ArchivingFileDTO> archivingFile = eArchivingFileService.getArchivingFiles(messageId.getUserMessageEntityId());
        getArch.stop();

        for (Map.Entry<String, ArchivingFileDTO> file : archivingFile.entrySet()) {
            LOG.trace("Process file [{}]", file.getKey());
            String relativePathToMessageFolder = IPConstants.DATA_FOLDER + messageId.getMessageId() + IPConstants.ZIP_PATH_SEPARATOR + file.getKey();

            com.codahale.metrics.Timer.Context getPath = metricRegistry.timer(name("addUserMessage", "getPath", "timer")).time();
            Path dir = Paths.get(batchDirectory.toFile().getAbsolutePath(), "representations", "representation1", "data", messageId.getMessageId());
            Path path = Paths.get(dir.toFile().getAbsolutePath(), file.getKey());
            getPath.stop();

            com.codahale.metrics.Timer.Context crtFile = metricRegistry.timer(name("addUserMessage", "createDataFile", "timer")).time();
            ArchivingFileDTO archivingFileDTO = file.getValue();
            archivingFileDTO.setPath(path);
            try (InputStream inputStream = archivingFileDTO.getInputStream()) {
                eArkSipBuilderService.createDataFile(path, inputStream);
            } catch (IOException e) {
                throw new DomibusEArchiveException("Could not createDataFile on dir [" + dir + "] and file [" + archivingFileDTO + "]", e);
            }
            crtFile.stop();
            com.codahale.metrics.Timer.Context mets = metricRegistry.timer(name("addUserMessage", "addDataFileInfoToMETS", "timer")).time();
            eArkSipBuilderService.addDataFileInfoToMETS(mainMETSWrapper, relativePathToMessageFolder, archivingFileDTO);
            mets.stop();

        }
    }

}
