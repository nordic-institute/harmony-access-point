package eu.domibus.core.earchive;

import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.helpers.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class FileSystemEArchivePersistence implements EArchivePersistence {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileSystemEArchivePersistence.class);

    @Autowired
    protected EArchiveFileStorageProvider storageProvider;

// TODO: François Gautier 01-09-21 will be needed later
//    @Autowired
//    protected BackendNotificationService backendNotificationService;
//
//    @Autowired
//    protected CompressionService compressionService;
//
//    @Autowired
//    protected EArchivePersistenceHelper eArchivePersistenceHelper;
//
//    @Autowired
//    protected PayloadEncryptionService encryptionService;


    @Override
    public void createEArkSipStructure(String batchId) {
        LOG.info("Create dummy structure for batchId [{}]", batchId);
        File batchDirectory = new File(storageProvider.getCurrentStorage().getStorageDirectory(), batchId);
        File representations = new File(batchDirectory, "representations");
        File representation1 = new File(representations, "representation" + 1);
        File data = new File(representation1, "data");
        File message1 = new File(data, "MESSAGE_" + 1);

        FileUtils.mkDir(batchDirectory);
        FileUtils.mkDir(representations);
        FileUtils.mkDir(representation1);
        FileUtils.mkDir(data);
        FileUtils.mkDir(message1);
    }
}
