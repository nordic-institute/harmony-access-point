/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/commons-ip
 */
package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.DivType.Fptr;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType.FLocat;
import org.roda_project.commons_ip2.model.*;
import org.roda_project.commons_ip2.model.impl.ModelUtils;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.Utils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Copied from org.roda_project.commons_ip2.model.impl.eark.EARKUtils
 */
public final class DomibusEARKUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEARKUtils.class);

    private DomibusEARKUtils() {
        // do nothing
    }

    protected static void addMetsFileToFolder(Path destinationDirectory, MetsWrapper mainMETSWrapper) throws IPException {
        try {
            METSUtils.marshallMETS(mainMETSWrapper.getMets(), Paths.get(destinationDirectory.toFile().getAbsolutePath(), IPConstants.METS_FILE), true);
        } catch (JAXBException | IOException e) {
            throw new DomibusEArchiveException("Could not create METS.xml to [" + destinationDirectory.toAbsolutePath() + "]", e);
        }
    }

    private static void addFileToFolder(DomibusIPFile file, Path path) {
        try {
            path.toFile().getParentFile().mkdirs();

            Files.copy(file.getFile(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not create file [" + file.getFileName() + "] to [" + path.toAbsolutePath() + "]", e);

        }
    }

    protected static void addRepresentationsToFolderAndMETS(IPInterface ip, List<IPRepresentation> representations,
                                                            MetsWrapper mainMETSWrapper, Path destinationDirectory)
            throws IPException, InterruptedException {
        // representations
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

    protected static void addRepresentationDataFilesToFolderAndMETS(IPInterface ip,
                                                                    MetsWrapper representationMETSWrapper,
                                                                    IPRepresentation representation,
                                                                    String representationId,
                                                                    Path destinationDirectory)
            throws IPException, InterruptedException {
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

    public static void addDataFileToMETS(MetsWrapper representationMETS, String dataFilePath, Path dataFile)
            throws IPException, InterruptedException {
        FileType file = new FileType();
        file.setID(Utils.generateRandomAndPrefixedUUID());

        // set mimetype, date creation, etc.
        METSUtils.setFileBasicInformation(LOG, dataFile, file);

        // add to file section
        FLocat fileLocation = METSUtils.createFileLocation(dataFilePath);
        file.getFLocat().add(fileLocation);
        representationMETS.getDataFileGroup().getFile().add(file);

        // add to struct map
        if (representationMETS.getDataDiv().getFptr().isEmpty()) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(representationMETS.getDataFileGroup());
            representationMETS.getDataDiv().getFptr().add(fptr);
        }
    }

}
