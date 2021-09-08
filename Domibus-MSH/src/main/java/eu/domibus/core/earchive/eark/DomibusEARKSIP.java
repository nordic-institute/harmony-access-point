/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/commons-ip
 */
package eu.domibus.core.earchive.eark;

import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.MetsType;
import org.roda_project.commons_ip2.model.MetsWrapper;
import org.roda_project.commons_ip2.model.impl.ModelUtils;
import org.roda_project.commons_ip2.model.impl.eark.EARKMETSUtils;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public class DomibusEARKSIP extends EARKSIP {
    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusEARKSIP.class);

    private String batchId;

    @Override
    public Path build(final Path destinationDirectory, final String fileNameWithoutExtension, final boolean onlyManifest)
            throws IPException, InterruptedException {
        try {

            MetsWrapper mainMETSWrapper = EARKMETSUtils.generateMETS(StringUtils.join(this.getIds(), " "),
                    this.getDescription(), this.getProfile(), true, Optional.ofNullable(this.getAncestors()), null,
                    this.getHeader(), this.getType(), this.getContentType(), null);


            MetsType.MetsHdr.MetsDocumentID value = new MetsType.MetsHdr.MetsDocumentID();
            value.setValue(batchId);
            mainMETSWrapper.getMets().getMetsHdr().setMetsDocumentID(value);


            DomibusEARKUtils.addRepresentationsToFolderAndMETS(this, getRepresentations(), mainMETSWrapper, destinationDirectory);

            DomibusEARKUtils.addMetsFileToFolder(destinationDirectory, mainMETSWrapper);

            return destinationDirectory;
        } catch (InterruptedException e) {
            ModelUtils.cleanUpUponInterrupt(LOGGER, destinationDirectory);
            throw e;
        }
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
