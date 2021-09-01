package eu.domibus.core.earchive;

import java.io.IOException;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    void createEArkSipStructure(String uuid) throws IOException;

}
