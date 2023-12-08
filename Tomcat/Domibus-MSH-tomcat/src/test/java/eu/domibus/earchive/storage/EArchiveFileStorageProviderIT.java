package eu.domibus.earchive.storage;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.earchive.storage.EArchiveFileStorage;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveFileStorageProviderIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageProviderIT.class);

    @Autowired
    EArchiveFileStorageProvider eArchiveFileStorageProvider;

    @Test
    public void storageNotAccessible() {
        removeAllStorage();
        try {
            eArchiveFileStorageProvider.getCurrentStorage();
            Assert.fail();
        } catch (DomibusCoreException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString(DomibusCoreErrorCode.DOM_001.getErrorCode()));
        }
    }

    private void removeAllStorage() {
        Map<Domain, EArchiveFileStorage>  instances = (Map<Domain, EArchiveFileStorage>) ReflectionTestUtils.getField(eArchiveFileStorageProvider, "instances");

        if(instances == null) {
            LOG.warn("(Map<Domain, EArchiveFileStorage>) ReflectionTestUtils.getField(eArchiveFileStorageProvider, \"instances\") not found");
            return;
        }
        Set<Domain> domains = instances.keySet();
        for (Domain domain : domains) {
            instances.remove(domain);
        }
    }
}
