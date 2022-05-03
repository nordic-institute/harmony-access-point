package eu.domibus.core.earchive.eark;

import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.DivType;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.mets_v1_12.beans.MetsType;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.roda_project.commons_ip2.model.impl.eark.EARKMETSUtils;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.Utils;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EARKSIPFileService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EARKSIPFileService.class);

    private static final String SHA256_CHECKSUMTYPE = "SHA-256";
    public static final String SHA_256 = "sha256:";

    @Timer(clazz = EARKSIPFileService.class, value = "earchive21_getMetsWrapper")
    @Counter(clazz = EARKSIPFileService.class, value = "earchive21_getMetsWrapper")
    public MetsWrapper getMetsWrapper(String artifactName, String displayVersion, String batchId) throws IPException {
        EARKSIP sip = new EARKSIP();
        sip.addCreatorSoftwareAgent(artifactName, displayVersion);

        MetsWrapper mainMETSWrapper = EARKMETSUtils.generateMETS(StringUtils.join(sip.getIds(), " "),
                displayVersion,
                sip.getProfile(),
                true,
                Optional.ofNullable(sip.getAncestors()),
                null,
                sip.getHeader(),
                sip.getType(),
                sip.getContentType(),
                null);
        setMetsDocumentID(mainMETSWrapper, batchId);
        return mainMETSWrapper;
    }

    @Timer(clazz = EARKSIPFileService.class, value = "earchive_createDataFile")
    @Counter(clazz = EARKSIPFileService.class, value = "earchive_createDataFile")
    public void createDataFile(Path path, InputStream value) {

        try {
            try (FileOutputStream fileOS = FileUtils.openOutputStream(path.toFile())) {
                IOUtils.copy(value, fileOS);
            }
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not create file [" + path.toFile().getAbsolutePath() + "]", e);
        }
    }

    @Timer(clazz = EARKSIPFileService.class, value = "earchive23_getChecksum")
    @Counter(clazz = EARKSIPFileService.class, value = "earchive23_getChecksum")
    public String getChecksum(Path path) {
        try {
            return SHA_256 + getChecksumSHA256(path);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not calculate the checksum of the manifest for path [" + path + "]", e);
        }
    }

    private void setMetsDocumentID(MetsWrapper mainMETSWrapper, String batchId) {
        MetsType.MetsHdr.MetsDocumentID value = new MetsType.MetsHdr.MetsDocumentID();
        value.setValue(batchId);
        mainMETSWrapper.getMets().getMetsHdr().setMetsDocumentID(value);
    }

    @Timer(clazz = EARKSIPFileService.class, value = "earchive22_addMetsFileToFolder")
    @Counter(clazz = EARKSIPFileService.class, value = "earchive22_addMetsFileToFolder")
    protected Path addMetsFileToFolder(Path destinationDirectory, MetsWrapper mainMETSWrapper) throws IPException {
        try {
            return METSUtils.marshallMETS(mainMETSWrapper.getMets(), Paths.get(destinationDirectory.toFile().getAbsolutePath(), IPConstants.METS_FILE), true);
        } catch (JAXBException | IOException e) {
            throw new DomibusEArchiveException("Could not create METS.xml to [" + destinationDirectory.toFile().getName() + "]", e);
        }
    }

    public void addBatchJsonToMETS(MetsWrapper representationMETS, String pathFromData) {
        addDataFileInfoToMETS(representationMETS, pathFromData, null);
    }

    public void addDataFileInfoToMETS(MetsWrapper representationMETS, String pathFromData, Path dataFile) {
        FileType file = new FileType();
        file.setID(Utils.generateRandomAndPrefixedUUID());

        // set mimetype, date creation, etc.
        setFileBasicInformation(dataFile, file);

        // add to file section
        FileType.FLocat fileLocation = METSUtils.createFileLocation(pathFromData);
        file.getFLocat().add(fileLocation);
        representationMETS.getDataFileGroup().getFile().add(file);

        // add to struct map
        if (representationMETS.getDataDiv().getFptr().isEmpty()) {
            DivType.Fptr fptr = new DivType.Fptr();
            fptr.setFILEID(representationMETS.getDataFileGroup());
            representationMETS.getDataDiv().getFptr().add(fptr);
        }
    }

    public void setFileBasicInformation(Path file, FileType fileType) {
        initMimeTypeInfo(file, fileType);
        initDateCreation(fileType, getFileName(file));
        if (file != null) {
            initSizeInfo(file, fileType);
            initChecksum(file, fileType);
        }
    }

    private String getFileName(@Nullable Path file) {
        return file == null ? FileSystemEArchivePersistence.BATCH_JSON : file.toFile().getName();
    }

    private void initSizeInfo(Path file, FileType fileType) {
//        try {
            LOG.debug("Setting file size [{}]", file);
            fileType.setSIZE(1L);
//            fileType.setSIZE(Files.size(file));
            LOG.debug("Done setting file size");
//        } catch (IOException e) {
//            throw new DomibusEArchiveException("Error getting file size [" + file.toFile().getName() + "]", e);
//        }
    }

    private void initDateCreation(FileType fileType, String name) {
        try {
            fileType.setCREATED(getDatatypeFactory().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now(ZoneOffset.UTC))));
        } catch (DatatypeConfigurationException e) {
            throw new DomibusEArchiveException("Error getting curent calendar [" + name + "]", e);
        }
    }

    private void initMimeTypeInfo(Path file, FileType fileType) {
        try {
            LOG.debug("Setting mimetype [{}]", file);
            fileType.setMIMETYPE(getFileMimetype(file));
            LOG.debug("Done setting mimetype");
        } catch (IOException e) {
            throw new DomibusEArchiveException("Error probing content-type [" + getFileName(file) + "]", e);
        }
    }

    private void initChecksum(Path file, FileType fileType) {
        // checksum
        String checksumSHA256;
        try {
            checksumSHA256 = getChecksumSHA256(file);
            LOG.debug("checksumSHA256 [{}] for file [{}]", checksumSHA256, file.toFile().getName());
            fileType.setCHECKSUM(checksumSHA256);
            fileType.setCHECKSUMTYPE(SHA256_CHECKSUMTYPE);
        } catch (IOException e) {
            fileType.setCHECKSUM("ERROR");
            LOG.error("Exception while calculating [{}] hash", SHA256_CHECKSUMTYPE, e);
        }
    }

    private String getChecksumSHA256(Path path) throws IOException {
        try (final FileInputStream inputStream = FileUtils.openInputStream(path.toFile())) {
            return DigestUtils.sha256Hex(inputStream);
        }
    }

    private DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance();
    }

    private String getFileMimetype(@Nullable Path file) throws IOException {
//        if (file == null) {
            return "application/octet-stream";
//        }
//        return Files.probeContentType(file);
    }

}
