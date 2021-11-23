package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.DivType;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.mets_v1_12.beans.MetsType;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.IPFile;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.roda_project.commons_ip2.model.impl.ModelUtils;
import org.roda_project.commons_ip2.model.impl.eark.EARKMETSUtils;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.Utils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EARKSIPBuilderService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EARKSIPBuilderService.class);

    private static final String SHA256_CHECKSUMTYPE = "SHA-256";
    public static final String SHA_256 = "sha256:";

    private final EArchivingFileService eArchivingFileService;

    public EARKSIPBuilderService(EArchivingFileService eArchivingFileService) {
        this.eArchivingFileService = eArchivingFileService;
    }

    public DomibusEARKSIPResult build(DomibusEARKSIP domibusEARKSIP, final FileObject destinationDirectory) throws IPException {
        MetsWrapper mainMETSWrapper = EARKMETSUtils.generateMETS(StringUtils.join(
                        domibusEARKSIP.getIds(), " "),
                domibusEARKSIP.getDescription(),
                domibusEARKSIP.getProfile(),
                true,
                Optional.ofNullable(domibusEARKSIP.getAncestors()),
                null,
                domibusEARKSIP.getHeader(),
                domibusEARKSIP.getType(),
                domibusEARKSIP.getContentType(),
                null);

        setMetsDocumentID(domibusEARKSIP, mainMETSWrapper);

        addRepresentationsToFolderAndMETS(domibusEARKSIP.getRepresentations(), mainMETSWrapper, destinationDirectory);

        String checksum = getChecksum(addMetsFileToFolder(destinationDirectory, mainMETSWrapper));
        updateBatchJson(domibusEARKSIP.getRepresentations(), destinationDirectory, checksum);
        return new DomibusEARKSIPResult(destinationDirectory.getPath(), checksum);
    }

    private void updateBatchJson(List<IPRepresentation> representations, FileObject destinationDirectory, String checksum) {
        for (IPRepresentation representation : representations) {
            for (IPFile file : representation.getData()) {
                updateBatchJson(destinationDirectory, checksum, representation, file);
            }
        }
    }

    private void updateBatchJson(FileObject destinationDirectory, String checksum, IPRepresentation representation, IPFile file) {
        if (StringUtils.equalsIgnoreCase(file.getFileName(), FileSystemEArchivePersistence.BATCH_JSON)) {
            String pathBatchJason = IPConstants.REPRESENTATIONS_FOLDER + representation.getObjectID() + IPConstants.ZIP_PATH_SEPARATOR
                    + getPathFromData(file);
            try (FileObject fileObject = destinationDirectory.resolveFile(pathBatchJason)) {
                insertChecksum(fileObject, checksum);
            } catch (FileSystemException e) {
                throw new DomibusEArchiveException("Batch.json not found for insert manifest checksum", e);
            }
        }
    }

    private void insertChecksum(FileObject file, String checksum) {
        BatchEArchiveDTO batchEArchiveDTO = getBatchEArchiveDTO(file);
        batchEArchiveDTO.setManifestChecksum(checksum);
        try (OutputStream fileOS = file.getContent().getOutputStream(false)) {
            IOUtils.copy(eArchivingFileService.getBatchFileJson(batchEArchiveDTO), fileOS);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not write new checksum in batch.json in file [" + file.getName() + "]", e);
        }
    }

    private BatchEArchiveDTO getBatchEArchiveDTO(FileObject file) {
        try {
            return eArchivingFileService.getBatchEArchiveDTO(file.getContent().getInputStream());
        } catch (FileSystemException e) {
            throw new DomibusEArchiveException("Could not parse BatchEArchiveDTO from file [" + file.getName() + "]", e);
        }
    }

    private String getChecksum(Path path) {
        try {
            return SHA_256 + getChecksumSHA256(path);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not calculate the checksum of the manifest", e);
        }
    }

    private void setMetsDocumentID(DomibusEARKSIP domibusEARKSIP, MetsWrapper mainMETSWrapper) {
        MetsType.MetsHdr.MetsDocumentID value = new MetsType.MetsHdr.MetsDocumentID();
        value.setValue(domibusEARKSIP.getBatchId());
        mainMETSWrapper.getMets().getMetsHdr().setMetsDocumentID(value);
    }

    protected Path addMetsFileToFolder(FileObject destinationDirectory, MetsWrapper mainMETSWrapper) throws IPException {
        try {
            return METSUtils.marshallMETS(mainMETSWrapper.getMets(), destinationDirectory.resolveFile(IPConstants.METS_FILE).getPath(), true);
        } catch (JAXBException | IOException e) {
            throw new DomibusEArchiveException("Could not create METS.xml to [" + destinationDirectory.getName() + "]", e);
        }
    }

    protected void addFileToFolder(DomibusIPFile ipFile, FileObject fileObject) {
        try {
            fileObject.createFile();
            try (OutputStream fileOS = fileObject.getContent().getOutputStream(true)) {
                IOUtils.copy(ipFile.getInputStream(), fileOS);
            }
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not create file [" + ipFile.getFileName() + "] to [" + fileObject + "]", e);
        }
    }

    protected void addRepresentationsToFolderAndMETS(
            List<IPRepresentation> representations,
            MetsWrapper mainMETSWrapper,
            FileObject destinationDirectory) {
        if (representations != null && !representations.isEmpty()) {
            for (IPRepresentation representation : representations) {
                addRepresentationDataFilesToFolderAndMETS(mainMETSWrapper, representation, representation.getObjectID(), destinationDirectory);
            }
        }
    }

    protected void addRepresentationDataFilesToFolderAndMETS(
            MetsWrapper representationMETSWrapper,
            IPRepresentation representation,
            String representationId,
            FileObject destinationDirectory) {
        if (CollectionUtils.isNotEmpty(representation.getData())) {
            for (IPFile file : representation.getData()) {

                String pathFromRepresentation = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
                        + getPathFromData(file);
                try (FileObject fileObject = destinationDirectory.resolveFile(pathFromRepresentation)) {
                    addFileToFolder((DomibusIPFile) file, fileObject);
                    addDataFileToMETS(representationMETSWrapper, (DomibusIPFile) file, fileObject);
                } catch (FileSystemException e) {
                    throw new DomibusEArchiveException("Could not access to the folder [" + destinationDirectory + "] and file [" + getPathFromData(file) + "]");
                }

            }
        }
    }

    private String getPathFromData(IPFile file) {
        return IPConstants.DATA_FOLDER + ModelUtils.getFoldersFromList(file.getRelativeFolders())
                + file.getFileName();
    }

    protected void addDataFileToMETS(MetsWrapper representationMETS, DomibusIPFile domibusIPFile, FileObject dataFile) {
        FileType file = new FileType();
        file.setID(Utils.generateRandomAndPrefixedUUID());

        // set mimetype, date creation, etc.
        setFileBasicInformation(domibusIPFile, dataFile, file);

        // add to file section
        FileType.FLocat fileLocation = METSUtils.createFileLocation(getPathFromData(domibusIPFile));
        file.getFLocat().add(fileLocation);
        representationMETS.getDataFileGroup().getFile().add(file);

        // add to struct map
        if (representationMETS.getDataDiv().getFptr().isEmpty()) {
            DivType.Fptr fptr = new DivType.Fptr();
            fptr.setFILEID(representationMETS.getDataFileGroup());
            representationMETS.getDataDiv().getFptr().add(fptr);
        }
    }

    public void setFileBasicInformation(DomibusIPFile domibusIPFile, FileObject file, FileType fileType) {
        initMimeTypeInfo(file, fileType);
        initDateCreation(file, fileType);
        initSizeInfo(file, fileType);
        initChecksum(domibusIPFile, file, fileType);
    }

    private void initSizeInfo(FileObject file, FileType fileType) {
        try {
            LOG.debug("Setting file size [{}]", file);
            fileType.setSIZE(file.getContent().getSize());
            LOG.debug("Done setting file size");
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error getting file size [" + file.getName() + "]", e);
        }
    }

    private void initDateCreation(FileObject file, FileType fileType) {
        try {
            fileType.setCREATED(getDatatypeFactory().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now(ZoneOffset.UTC))));
        } catch (DatatypeConfigurationException e) {
            throw new DomibusEArchiveException("Error getting curent calendar [" + file.getName() + "]", e);
        }
    }

    private void initMimeTypeInfo(FileObject file, FileType fileType) {
        try {
            LOG.debug("Setting mimetype [{}]", file);
            fileType.setMIMETYPE(getFileMimetype(file));
            LOG.debug("Done setting mimetype");
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error probing content-type [" + file.getName() + "]", e);
        }
    }

    private void initChecksum(DomibusIPFile domibusIPFile, FileObject file, FileType fileType) {
        if (domibusIPFile.writeChecksum()) {
            // checksum
            String checksumSHA256;
            try {
                Path path = file.getPath();
                checksumSHA256 = getChecksumSHA256(path);
                LOG.debug("checksumSHA256 [{}] for file [{}]", checksumSHA256, file.getName());
                fileType.setCHECKSUM(checksumSHA256);
                fileType.setCHECKSUMTYPE(SHA256_CHECKSUMTYPE);
            } catch (IOException e) {
                fileType.setCHECKSUM("ERROR");
                LOG.error("Exception while calculating [{}] hash", SHA256_CHECKSUMTYPE, e);
            }
        } else {
            LOG.debug("no checkSum for file [{}]", domibusIPFile.getFileName());
        }
    }

    private String getChecksumSHA256(Path path) throws IOException {
        return DigestUtils.sha256Hex(Files.newInputStream(path));
    }

    private DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance();
    }

    private String getFileMimetype(FileObject file) throws IOException {
        String probedContentType = file.getContent().getContentInfo().getContentType();
        if (probedContentType == null) {
            probedContentType = "application/octet-stream";
        }
        return probedContentType;
    }

}
