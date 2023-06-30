package eu.domibus.earchive.storage;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveFileStorageProviderIT extends AbstractIT {

    @Autowired
    EArchiveFileStorageProvider eArchiveFileStorageProvider;

    @Test
    public void storageNotAccessible() {
        try {
            eArchiveFileStorageProvider.getCurrentStorage();
            Assert.fail();
        } catch (DomibusCoreException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString(DomibusCoreErrorCode.DOM_001.getErrorCode()));
        }
    }
}
