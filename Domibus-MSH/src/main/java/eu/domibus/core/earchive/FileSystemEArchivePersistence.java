package eu.domibus.core.earchive;

import eu.domibus.core.earchive.eark.DomibusEARKSIP;
import eu.domibus.core.earchive.eark.DomibusIPFile;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.helpers.FileUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.SIP;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
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

    private final EArchivingService eArchivingService;

    public FileSystemEArchivePersistence(EArchiveFileStorageProvider storageProvider,
                                         DomibusVersionService domibusVersionService,
                                         EArchivingService eArchivingService) {
        this.storageProvider = storageProvider;
        this.domibusVersionService = domibusVersionService;
        this.eArchivingService = eArchivingService;
    }

    @Override
    public Path createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO) {
        LOG.info("Create dummy structure for batchId [{}]", batchEArchiveDTO.getBatchId());

        File batchDirectory = new File(storageProvider.getCurrentStorage().getStorageDirectory(), batchEArchiveDTO.getBatchId());
        FileUtils.mkDir(batchDirectory);

        try {
            DomibusEARKSIP sip = new DomibusEARKSIP();
            sip.setBatchId(batchEArchiveDTO.getBatchId());
            sip.addCreatorSoftwareAgent(domibusVersionService.getArtifactName(), domibusVersionService.getDisplayVersion());
            sip.setDescription(domibusVersionService.getDisplayVersion());

            representation1(sip, batchEArchiveDTO);

            return sip.build(batchDirectory.toPath());
        } catch (IPException | InterruptedException e) {
            throw new DomibusEArchiveException("createEArkSipStructure could not execute", e);
        }
    }

    private void representation1(SIP sip, BatchEArchiveDTO batchEArchiveDTO) throws IPException {
        IPRepresentation representation1 = new IPRepresentation("representation1");
        sip.addRepresentation(representation1);

        InputStream batchFileJson = eArchivingService.getBatchFileJson(batchEArchiveDTO);
        representation1.addFile(new DomibusIPFile(batchFileJson, BATCH_JSON));
        for (String messageId : batchEArchiveDTO.getMessages()) {
            addUserMessage(representation1, messageId);
        }
    }

    private void addUserMessage(IPRepresentation representation1, String messageId) {
        Map<String, InputStream> archivingFile = eArchivingService.getArchivingFiles(messageId);

        for (Map.Entry<String, InputStream> stringInputStreamEntry : archivingFile.entrySet()) {
            processFile(representation1, messageId, stringInputStreamEntry);
        }
    }

    private void processFile(IPRepresentation representation1, String messageId, Map.Entry<String, InputStream> aFile) {
        DomibusIPFile soapEnvelope = new DomibusIPFile(aFile.getValue(), aFile.getKey());
        soapEnvelope.setRelativeFolders(singletonList(messageId));
        representation1.addFile(soapEnvelope);
    }
}
