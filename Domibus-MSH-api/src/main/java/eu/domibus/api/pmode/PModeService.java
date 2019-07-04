package eu.domibus.api.pmode;

import eu.domibus.api.pmode.domain.LegConfiguration;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface PModeService {

    LegConfiguration getLegConfiguration(String messageId) throws PModeException;

    byte[] getPModeFile(int id);

    PModeArchiveInfo getCurrentPMode();

    List<String> updatePModeFile(byte[] bytes, String description) throws PModeException;
}
