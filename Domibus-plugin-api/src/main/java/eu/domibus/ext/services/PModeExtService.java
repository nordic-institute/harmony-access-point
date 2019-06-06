package eu.domibus.ext.services;

import eu.domibus.ext.domain.PModeArchiveInfoDTO;

/**
 * All operations related to PMode files
 *
 * <ul>
 * <li>get current PMode file information</li>
 * <li>download the PMode file</li>
 * <ul/>
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public interface PModeExtService {

    /**
     * Get PMode file as {@code byte[]}
     *
     * @param id id of the PMode to download/get
     * @return array of bytes
     */
    byte[] getPModeFile(int id);

    /**
     * Returns PMode current file information
     *
     * @return an instance of {@code PModeArchiveInfoDTO}
     */
    PModeArchiveInfoDTO getCurrentPmode();

}
