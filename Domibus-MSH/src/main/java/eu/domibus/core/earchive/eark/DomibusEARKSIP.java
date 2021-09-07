/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/commons-ip
 */
package eu.domibus.core.earchive.eark;

import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip.utils.ZipEntryInfo;
import org.roda_project.commons_ip2.mets_v1_12.beans.MetsType;
import org.roda_project.commons_ip2.mets_v1_12.beans.StructMapType;
import org.roda_project.commons_ip2.model.*;
import org.roda_project.commons_ip2.model.impl.ModelUtils;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.ZIPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DomibusEARKSIP extends SIP {
  private static final Logger LOGGER = LoggerFactory.getLogger(DomibusEARKSIP.class);

  private static final String SIP_TEMP_DIR = "EARKSIP";
  private static final String SIP_FILE_EXTENSION = ".zip";

  private String batchId;

  public DomibusEARKSIP() {
    super();
    setProfile(IPConstants.COMMON_SPEC_PROFILE);
  }

  /**
   * @param sipId
   *          will be used as OBJID in METS (/mets[@OBJID])
   */
  public DomibusEARKSIP(String sipId) {
    super(sipId);
    setProfile(IPConstants.COMMON_SPEC_PROFILE);
  }

  /**
   * @param sipId
   *          will be used as OBJID in METS (/mets[@OBJID])
   */
  public DomibusEARKSIP(String sipId, IPContentType contentType, IPContentInformationType contentInformationType) {
    super(sipId, contentType, contentInformationType);
    setProfile(IPConstants.COMMON_SPEC_PROFILE);
  }

  /**
   * 
   * build and all build related methods
   * _________________________________________________________________________
   */
  @Override
  public Path build(Path destinationDirectory) throws IPException, InterruptedException {
    return build(destinationDirectory, null);
  }

  /**
   * Builds a SIP.
   *
   * @param destinationDirectory
   *          the {@link Path} where the SIP should be build.
   * @param onlyManifest
   *          build only the manifest file? (<strong>this parameter is
   *          ignored</strong>).
   * @return the {@link Path}.
   * @throws IPException
   *           if some error occurs.
   * @throws InterruptedException
   *           if some error occurs.
   */
  @Override
  public Path build(final Path destinationDirectory, final boolean onlyManifest)
    throws IPException, InterruptedException {
    return build(destinationDirectory, null);
  }

  @Override
  public Path build(Path destinationDirectory, String fileNameWithoutExtension)
    throws IPException, InterruptedException {
    return build(destinationDirectory, fileNameWithoutExtension, false);
  }

  /**
   *
   * build and all build related methods
   * _________________________________________________________________________
   */
  /**
   * Builds a SIP.
   *
   * @param destinationDirectory
   *          the {@link Path} where the SIP should be build.
   * @param fileNameWithoutExtension
   *          the name of the output file without extension.
   * @param onlyManifest
   *          build only the manifest file? (<strong>this parameter is
   *          ignored</strong>).
   * @return the {@link Path}.
   * @throws IPException
   *           if some error occurs.
   * @throws InterruptedException
   *           if some error occurs.
   */
  @Override
  public Path build(final Path destinationDirectory, final String fileNameWithoutExtension, final boolean onlyManifest)
    throws IPException, InterruptedException {
    IPConstants.METS_ENCODE_AND_DECODE_HREF = true;
    Path buildDir = ModelUtils.createBuildDir(SIP_TEMP_DIR);
    Path zipPath = getZipPath(destinationDirectory, fileNameWithoutExtension);
    try {
      Map<String, ZipEntryInfo> zipEntries = getZipEntries();

      MetsWrapper mainMETSWrapper = DomibusEARKMETSUtils.generateMETS(StringUtils.join(this.getIds(), " "),
        this.getDescription(), this.getProfile(), true, Optional.ofNullable(this.getAncestors()), null,
        this.getHeader(), this.getType(), this.getContentType(), null);

      DomibusEARKUtils.addDescriptiveMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getDescriptiveMetadata(), null);
      DomibusEARKUtils.addPreservationMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getPreservationMetadata(), null);
      DomibusEARKUtils.addOtherMetadataToZipAndMETS(zipEntries, mainMETSWrapper, getOtherMetadata(), null);
      DomibusEARKUtils.addRepresentationsToZipAndMETS(this, getRepresentations(), zipEntries, mainMETSWrapper, buildDir);
      DomibusEARKUtils.addDefaultSchemas(LOGGER, getSchemas(), buildDir);
      DomibusEARKUtils.addSchemasToZipAndMETS(zipEntries, mainMETSWrapper, getSchemas(), null);
      DomibusEARKUtils.addDocumentationToZipAndMETS(zipEntries, mainMETSWrapper, getDocumentation(), null);
      METSUtils.addMainMETSToZip(zipEntries, mainMETSWrapper, buildDir);

      MetsType.MetsHdr.MetsDocumentID value = new MetsType.MetsHdr.MetsDocumentID();
      value.setValue(batchId);
      mainMETSWrapper.getMets().getMetsHdr().setMetsDocumentID(value);
      createZipFile(zipEntries, zipPath);
      return zipPath;
    } catch (InterruptedException e) {
      ModelUtils.cleanUpUponInterrupt(LOGGER, zipPath);
      throw e;
    } finally {
      ModelUtils.deleteBuildDir(buildDir);
    }
  }

  private Path getZipPath(Path destinationDirectory, String fileNameWithoutExtension) throws IPException {
    Path zipPath;
    if (fileNameWithoutExtension != null) {
      zipPath = destinationDirectory.resolve(fileNameWithoutExtension + SIP_FILE_EXTENSION);
    } else {
      zipPath = destinationDirectory.resolve(getId() + SIP_FILE_EXTENSION);
    }

    try {
      if (Files.exists(zipPath)) {
        Files.delete(zipPath);
      }
    } catch (IOException e) {
      throw new IPException("Error deleting already existing zip", e);
    }
    return zipPath;
  }

  private void createZipFile(Map<String, ZipEntryInfo> zipEntries, Path zipPath)
    throws IPException, InterruptedException {
    try {
      notifySipBuildPackagingStarted(zipEntries.size());
      ZIPUtils.zip(zipEntries, Files.newOutputStream(zipPath), this, false, true);
    } catch (ClosedByInterruptException e) {
      throw new InterruptedException();
    } catch (IOException e) {
      throw new IPException("Error generating E-ARK SIP ZIP file. Reason: " + e.getMessage(), e);
    } finally {
      notifySipBuildPackagingEnded();
    }
  }

  /**
   * 
   * parse and all parse related methods; during parse, validation is also
   * conducted and stored inside the SIP
   * _________________________________________________________________________
   */

  public static SIP parse(Path source, Path destinationDirectory) throws ParseException {
    return parseEARKSIP(source, destinationDirectory);
  }

  public static SIP parse(Path source) throws ParseException {
    try {
      return parse(source, Files.createTempDirectory("unzipped"));
    } catch (IOException e) {
      throw new ParseException("Error creating temporary directory for E-ARK SIP parse", e);
    }
  }

  private static SIP parseEARKSIP(final Path source, final Path destinationDirectory) throws ParseException {
    try {
      IPConstants.METS_ENCODE_AND_DECODE_HREF = true;
      SIP sip = new DomibusEARKSIP();

      Path sipPath = ZIPUtils.extractIPIfInZipFormat(source, destinationDirectory);
      sip.setBasePath(sipPath);

      MetsWrapper metsWrapper = DomibusEARKUtils.processMainMets(sip, sipPath);

      if (sip.isValid()) {

        StructMapType structMap = DomibusEARKUtils.getEARKStructMap(metsWrapper, sip, true);

        if (structMap != null) {
          DomibusEARKUtils.preProcessStructMap(metsWrapper, structMap);
          DomibusEARKUtils.processDescriptiveMetadata(metsWrapper, sip, LOGGER, null, sip.getBasePath());
          DomibusEARKUtils.processOtherMetadata(metsWrapper, sip, LOGGER, null, sip.getBasePath());
          DomibusEARKUtils.processPreservationMetadata(metsWrapper, sip, LOGGER, null, sip.getBasePath());
          DomibusEARKUtils.processRepresentations(metsWrapper, sip, LOGGER);
          DomibusEARKUtils.processSchemasMetadata(metsWrapper, sip, sip.getBasePath());
          DomibusEARKUtils.processDocumentationMetadata(metsWrapper, sip, sip.getBasePath());
          DomibusEARKUtils.processAncestors(metsWrapper, sip);
        }
      }

      return sip;
    } catch (final IPException e) {
      throw new ParseException("Error parsing E-ARK SIP", e);
    }
  }

  @Override
  public Set<String> getExtraChecksumAlgorithms() {
    return Collections.emptySet();
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }
}
