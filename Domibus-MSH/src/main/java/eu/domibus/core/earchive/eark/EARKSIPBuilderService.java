package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.DivType;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.mets_v1_12.beans.MetsType;
import org.roda_project.commons_ip2.model.*;
import org.roda_project.commons_ip2.model.impl.ModelUtils;
import org.roda_project.commons_ip2.model.impl.eark.EARKMETSUtils;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.Utils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EARKSIPBuilderService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EARKSIPBuilderService.class);

    public Path build(DomibusEARKSIP domibusEARKSIP,
                      final Path destinationDirectory)
            throws IPException, InterruptedException {

        try {
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

            addRepresentationsToFolderAndMETS(domibusEARKSIP, domibusEARKSIP.getRepresentations(), mainMETSWrapper, destinationDirectory);

            addMetsFileToFolder(destinationDirectory, mainMETSWrapper);

            return destinationDirectory;
        } catch (InterruptedException e) {
            ModelUtils.cleanUpUponInterrupt(LOG, destinationDirectory);
            throw e;
        }
    }

    private void setMetsDocumentID(DomibusEARKSIP domibusEARKSIP, MetsWrapper mainMETSWrapper) {
        MetsType.MetsHdr.MetsDocumentID value = new MetsType.MetsHdr.MetsDocumentID();
        value.setValue(domibusEARKSIP.getBatchId());
        mainMETSWrapper.getMets().getMetsHdr().setMetsDocumentID(value);
    }

    protected void addMetsFileToFolder(Path destinationDirectory, MetsWrapper mainMETSWrapper) throws IPException {
        try {
            METSUtils.marshallMETS(mainMETSWrapper.getMets(), Paths.get(destinationDirectory.toFile().getAbsolutePath(), IPConstants.METS_FILE), true);
        } catch (JAXBException | IOException e) {
            throw new DomibusEArchiveException("Could not create METS.xml to [" + destinationDirectory.toAbsolutePath() + "]", e);
        }
    }

    protected void addFileToFolder(DomibusIPFile file, Path path) {
        try {
            path.toFile().getParentFile().mkdirs();

            Files.copy(file.getFile(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not create file [" + file.getFileName() + "] to [" + path.toAbsolutePath() + "]", e);
        }
    }

    protected void addRepresentationsToFolderAndMETS(
            IPInterface ip,
            List<IPRepresentation> representations,
            MetsWrapper mainMETSWrapper,
            Path destinationDirectory) throws IPException, InterruptedException {
        if (representations != null && !representations.isEmpty()) {
            if (ip instanceof SIP) {
                ((SIP) ip).notifySipBuildRepresentationsProcessingStarted(representations.size());
            }
            for (IPRepresentation representation : representations) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                addRepresentationDataFilesToFolderAndMETS(ip, mainMETSWrapper, representation, representation.getObjectID(), destinationDirectory);

            }
            if (ip instanceof SIP) {
                ((SIP) ip).notifySipBuildRepresentationsProcessingEnded();
            }
        }
    }

    protected void addRepresentationDataFilesToFolderAndMETS(
            IPInterface ip,
            MetsWrapper representationMETSWrapper,
            IPRepresentation representation,
            String representationId,
            Path destinationDirectory) throws IPException, InterruptedException {
        if (representation.getData() != null && !representation.getData().isEmpty()) {
            if (ip instanceof SIP) {
                ((SIP) ip).notifySipBuildRepresentationProcessingStarted(representation.getData().size());
            }
            int i = 0;
            for (IPFile file : representation.getData()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                String dataFilePath = IPConstants.DATA_FOLDER + ModelUtils.getFoldersFromList(file.getRelativeFolders())
                        + file.getFileName();
                addDataFileToMETS(representationMETSWrapper, dataFilePath, destinationDirectory);

                dataFilePath = IPConstants.REPRESENTATIONS_FOLDER + representationId + IPConstants.ZIP_PATH_SEPARATOR
                        + dataFilePath;
                addFileToFolder((DomibusIPFile) file, Paths.get(destinationDirectory.toFile().getAbsolutePath(), dataFilePath));

                i++;
                if (ip instanceof SIP) {
                    ((SIP) ip).notifySipBuildRepresentationProcessingCurrentStatus(i);
                }
            }
            if (ip instanceof SIP) {
                ((SIP) ip).notifySipBuildRepresentationProcessingEnded();
            }
        }
    }

    protected void addDataFileToMETS(MetsWrapper representationMETS, String dataFilePath, Path dataFile)
            throws IPException, InterruptedException {
        FileType file = new FileType();
        file.setID(Utils.generateRandomAndPrefixedUUID());

        // set mimetype, date creation, etc.
        METSUtils.setFileBasicInformation(LOG, dataFile, file);

        // add to file section
        FileType.FLocat fileLocation = METSUtils.createFileLocation(dataFilePath);
        file.getFLocat().add(fileLocation);
        representationMETS.getDataFileGroup().getFile().add(file);

        // add to struct map
        if (representationMETS.getDataDiv().getFptr().isEmpty()) {
            DivType.Fptr fptr = new DivType.Fptr();
            fptr.setFILEID(representationMETS.getDataFileGroup());
            representationMETS.getDataDiv().getFptr().add(fptr);
        }
    }
}
