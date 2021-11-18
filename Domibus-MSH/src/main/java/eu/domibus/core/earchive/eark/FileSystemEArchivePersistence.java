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
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.SIP;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class FileSystemEArchivePersistence implements EArchivePersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistence.class);
    public static final String BATCH_JSON = "batch.json";

    protected final EArchiveFileStorageProvider storageProvider;

    protected final DomibusVersionService domibusVersionService;

    private final EArchivingFileService eArchivingFileService;

    private final EARKSIPBuilderService eArkSipBuilderService;


    public FileSystemEArchivePersistence(EArchiveFileStorageProvider storageProvider,
                                         DomibusVersionService domibusVersionService,
                                         EArchivingFileService eArchivingFileService,
                                         EARKSIPBuilderService eArkSipBuilderService) {
        this.storageProvider = storageProvider;
        this.domibusVersionService = domibusVersionService;
        this.eArchivingFileService = eArchivingFileService;
        this.eArkSipBuilderService = eArkSipBuilderService;
    }

    @Override
    public FileObject createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds) {
        LOG.info("Create earchive structure for batchId [{}] with [{}] messages", batchEArchiveDTO.getBatchId(), userMessageEntityIds.size());

        try (FileObject batchDirectory = VFS.getManager().resolveFile(storageProvider.getCurrentStorage().getStorageDirectory(), batchEArchiveDTO.getBatchId())) {
            batchDirectory.createFolder();

            DomibusEARKSIP sip = new DomibusEARKSIP();
            sip.setBatchId(batchEArchiveDTO.getBatchId());
            sip.addCreatorSoftwareAgent(domibusVersionService.getArtifactName(), domibusVersionService.getDisplayVersion());
            sip.setDescription(domibusVersionService.getDisplayVersion());

            LOG.debug("DomibusEARKSIP initialized [{}]", sip);
            addRepresentation1(sip, batchEArchiveDTO, userMessageEntityIds);
            LOG.debug("DomibusEARKSIP created [{}]", sip);

            return eArkSipBuilderService.build(sip, batchDirectory);
        } catch (IPException | FileSystemException e) {
            throw new DomibusEArchiveException("Could not create eArchiving structure for batch [" + batchEArchiveDTO + "]", e);
        }
    }

    protected void addRepresentation1(SIP sip, BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds) throws IPException {
        IPRepresentation representation1 = new IPRepresentation("representation1");
        sip.addRepresentation(representation1);

        LOG.debug("Add batch.json");
        InputStream batchFileJson = eArchivingFileService.getBatchFileJson(batchEArchiveDTO);
        representation1.addFile(new DomibusIPFile(batchFileJson, BATCH_JSON));
        for (EArchiveBatchUserMessage messageId : userMessageEntityIds) {
            LOG.debug("Add messageId [{}]", messageId);
            addUserMessage(representation1, messageId);
        }
    }

    private void addUserMessage(IPRepresentation representation1, EArchiveBatchUserMessage messageId) {
        Map<String, InputStream> archivingFile = eArchivingFileService.getArchivingFiles(messageId.getUserMessageEntityId());

        for (Map.Entry<String, InputStream> file : archivingFile.entrySet()) {
            LOG.trace("Process file [{}]", file.getKey());
            processFile(representation1, messageId.getMessageId(), file);
        }
    }

    private void processFile(IPRepresentation representation1, String messageId, Map.Entry<String, InputStream> aFile) {
        DomibusIPFile soapEnvelope = new DomibusIPFile(aFile.getValue(), aFile.getKey());
        soapEnvelope.setRelativeFolders(singletonList(messageId));
        representation1.addFile(soapEnvelope);
    }
}
