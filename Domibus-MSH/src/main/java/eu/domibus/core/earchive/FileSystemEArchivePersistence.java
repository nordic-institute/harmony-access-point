package eu.domibus.core.earchive;

import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.helpers.FileUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip2.model.*;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
        FileUtils.mkDir(batchDirectory);

        try {
            SIP sip = new EARKSIP("SIP_1", IPContentType.getMIXED(), IPContentInformationType.getMIXED());
            sip.addCreatorSoftwareAgent("RODA Commons IP", "2.0.0");

// 1.1) set optional human-readable description
            sip.setDescription("A full E-ARK SIP");

// 1.2) add descriptive metadata (SIP level)
            IPDescriptiveMetadata metadataDescriptiveDC = new IPDescriptiveMetadata(
                    new IPFile(Paths.get("src/test/resources/eark/metadata_descriptive_dc.xml")),
                    new MetadataType(MetadataType.MetadataTypeEnum.DC), null);

            sip.addDescriptiveMetadata(metadataDescriptiveDC);


// 1.3) add preservation metadata (SIP level)
            IPMetadata metadataPreservation = new IPMetadata(
                    new IPFile(Paths.get("src/test/resources/eark/metadata_preservation_premis.xml")));
            sip.addPreservationMetadata(metadataPreservation);

// 1.4) add other metadata (SIP level)
            IPFile metadataOtherFile = new IPFile(Paths.get("src/test/resources/eark/metadata_other.txt"));
// 1.4.1) optionally one may rename file final name
            metadataOtherFile.setRenameTo("metadata_other_renamed.txt");
            IPMetadata metadataOther = new IPMetadata(metadataOtherFile);
            sip.addOtherMetadata(metadataOther);

// 1.5) add xml schema (SIP level)
            sip.addSchema(new IPFile(Paths.get("src/test/resources/eark/schema.xsd")));

// 1.6) add documentation (SIP level)
            sip.addDocumentation(new IPFile(Paths.get("src/test/resources/eark/documentation.pdf")));

// 1.7) set optional RODA related information about ancestors
            sip.setAncestors(Arrays.asList("b6f24059-8973-4582-932d-eb0b2cb48f28"));

// 1.8) add an agent (SIP level)
            IPAgent agent = new IPAgent("Agent Name", "OTHER", "OTHER ROLE", METSEnums.CreatorType.INDIVIDUAL, "OTHER TYPE", "",
                    IPAgentNoteTypeEnum.SOFTWARE_VERSION);
            sip.addAgent(agent);

// 1.9) add a representation (status will be set to the default value, i.e.,
// ORIGINAL)
            IPRepresentation representation1 = new IPRepresentation("representation 1");
            sip.addRepresentation(representation1);

// 1.9.1) add a file to the representation
            IPFile representationFile = new IPFile(Paths.get("src/test/resources/eark/documentation.pdf"));
            representationFile.setRenameTo("data.pdf");
            representation1.addFile(representationFile);

// 1.9.2) add a file to the representation and put it inside a folder
// called 'def' which is inside a folder called 'abc'
            IPFile representationFile2 = new IPFile(Paths.get("src/test/resources/eark/documentation.pdf"));
            representationFile2.setRelativeFolders(Arrays.asList("abc", "def"));
            representation1.addFile(representationFile2);

// 1.10) add a representation & define its status
            IPRepresentation representation2 = new IPRepresentation("representation 2");
            representation2.setStatus(new RepresentationStatus(RepresentationStatus.RepresentationStatusEnum.ORIGINAL));//REPRESENTATION_STATUS_NORMALIZED
            sip.addRepresentation(representation2);

// 1.10.1) add a file to the representation
            IPFile representationFile3 = new IPFile(Paths.get("src/test/resources/eark/documentation.pdf"));
            representationFile3.setRenameTo("data3.pdf");
            representation2.addFile(representationFile3);

// 2) build SIP, providing an output directory
            Path zipSIP = sip.build(batchDirectory.toPath());
        } catch (IPException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
