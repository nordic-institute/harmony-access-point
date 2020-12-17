package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageFactory;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProviderImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class StorageProviderImplTest {

    @Injectable
    protected PayloadFileStorageFactory storageFactory;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    Map<Domain, PayloadFileStorage> instances = new HashMap<>();

    @Tested
    PayloadFileStorageProviderImpl storageProvider;

    @Test
    public void init(@Injectable PayloadFileStorage storage) {
        List<Domain> domains = new ArrayList<>();
        final Domain domain = DomainService.DEFAULT_DOMAIN;
        domains.add(domain);

        new Expectations() {{
            domainService.getDomains();
            result = domains;

            storageFactory.create(domain);
            result = storage;
        }};

        storageProvider.init();

        new Verifications() {{
            storageFactory.create(domain);
            times = 1;

            instances.put(domain, storage);
        }};
    }

    @Test
    public void forDomain() {
        final Domain domain = DomainService.DEFAULT_DOMAIN;

        storageProvider.forDomain(domain);

        new Verifications() {{
            instances.get(domain);
        }};
    }

    @Test
    public void getCurrentStorage(@Injectable PayloadFileStorage storage) {
        final Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations(storageProvider) {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            storageProvider.forDomain(domain);
            result = storage;
        }};

        final PayloadFileStorage currentStorage = storageProvider.getCurrentStorage();
        Assert.assertEquals(currentStorage, storage);
    }

    @Test
    public void savePayloadsInDatabase(@Injectable PayloadFileStorage storage) {
        new Expectations(storageProvider) {{
            storageProvider.getCurrentStorage();
            result = storage;

            storage.getStorageDirectory();
            result = null;
        }};

        Assert.assertTrue(storageProvider.isPayloadsPersistenceInDatabaseConfigured());
    }

    @Test
    public void testSavePayloadsInDatabaseWithFileSystemStorage(@Injectable PayloadFileStorage storage,
                                                                @Injectable File file) {
        new Expectations(storageProvider) {{
            storageProvider.getCurrentStorage();
            result = storage;

            storage.getStorageDirectory();
            result = file;

            file.getName();
            result = "/home/storage";
        }};

        Assert.assertFalse(storageProvider.isPayloadsPersistenceInDatabaseConfigured());
    }
}