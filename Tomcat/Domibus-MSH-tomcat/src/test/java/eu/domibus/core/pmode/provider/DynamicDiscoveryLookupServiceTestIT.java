package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupDao;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryLookupService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.PKIUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class DynamicDiscoveryLookupServiceTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupServiceTestIT.class);

    @Autowired
    protected DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;

    @Autowired
    protected DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    @Before
    public void initialize() {
        deleteAllEntriesFromDB();
    }

    @After
    public void clean() {
        deleteAllEntriesFromDB();
    }

    protected void deleteAllEntriesFromDB() {
        //we delete all data from DB to check if the URL is retrieved from the cache
        dynamicDiscoveryLookupDao.deleteAll(dynamicDiscoveryLookupDao.findAll());
    }

    @Transactional
    @Test
    public void saveFinalRecipientEndpoint() {
        final String finalRecipient = "0208:111";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        String partyName = "myPartyName";
        String partyType = "myPartyType";
        Long certificateSerialNumber = 1111L;

        final PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate partyCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(certificateSerialNumber), "CN=" + partyName + ",OU=Domibus,O=eDelivery,C=EU");
        dynamicDiscoveryLookupService.saveDynamicDiscoveryLookupTime(finalRecipient, endpointUrl, partyName, partyType, Arrays.asList("myProcess"), partyName, partyCertificate);

        final List<DynamicDiscoveryLookupEntity> dynamicDiscoveryLookupEntities = dynamicDiscoveryLookupDao.findAll();
        assertEquals(1, dynamicDiscoveryLookupEntities.size());

        //we clear the cache
        dynamicDiscoveryLookupService.clearFinalRecipientAccessPointUrlsCache();

        //the final recipient URL should be retrieved from database and added in the cache
        assertEquals(endpointUrl, dynamicDiscoveryLookupService.getEndpointURL(finalRecipient));

        //we delete all data from DB to check if the URL is retrieved from the cache
        deleteAllEntriesFromDB();

        //the final recipient URL should be retrieved from the cache
        assertEquals(endpointUrl, dynamicDiscoveryLookupService.getEndpointURL(finalRecipient));
    }

    @Test
    public void testUniqueConstraintViolation() {
        final DynamicDiscoveryLookupEntity lookupEntity = createNewDynamicDiscoveryLookupEntityInstance();

        dynamicDiscoveryLookupDao.createOrUpdate(lookupEntity);

        //we create the same instance and we try to save it
        final DynamicDiscoveryLookupEntity lookupEntity2 = createNewDynamicDiscoveryLookupEntityInstance();

        try {
            dynamicDiscoveryLookupDao.createOrUpdate(lookupEntity2);
            fail("It should have triggered a unique constraint violation");
        } catch (DataIntegrityViolationException e) {
            //in a cluster environment, an entity associated for a final recipient can be created in parallel and a unique constraint is raised
            //in case a constraint violation occurs we don't do anything because the other node added the latest data in parallel
            LOG.warn("Could not create or update lookup entity with entity id [{}]. It could be that another node updated the same entity in parallel", lookupEntity2.getEntityId(), e);
        }
    }

    protected DynamicDiscoveryLookupEntity createNewDynamicDiscoveryLookupEntityInstance() {
        String finalRecipientValue = "myFinalRecipient";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        String partyName = "myPartyName";
        String partyType = "myPartyType";
        String certificateSerialNumber = "1111";
        final List<String> myProcess = Arrays.asList("myProcess");
        String issuerSubject = "myIssuer";
        String cn = "myCn";
        String subject = "mySubject";
        String serial = "mySerial";
        String fingerprint = "myFingerprint";
        Date dynamicDiscoveryTime = new Date();

        DynamicDiscoveryLookupEntity result = new DynamicDiscoveryLookupEntity();
        result.setFinalRecipientValue(finalRecipientValue);
        result.setFinalRecipientUrl(endpointUrl);
        result.setPartyName(partyName);
        result.setPartyType(partyType);
        result.setSerial(certificateSerialNumber);
        result.setPartyProcesses(myProcess);
        result.setIssuerSubject(issuerSubject);
        result.setCn(cn);
        result.setSubject(subject);
        result.setSerial(serial);
        result.setFingerprint(fingerprint);
        result.setDynamicDiscoveryTime(dynamicDiscoveryTime);

        return result;
    }


    @Transactional
    @Test
    public void deleteFinalRecipients() {
        final String finalRecipient = "0208:111";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        String partyName = "myPartyName";
        String partyType = "myPartyType";
        Long certificateSerialNumber = 1111L;

        final PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate partyCertificate = pkiUtil.createCertificateWithSubject(BigInteger.valueOf(certificateSerialNumber), "CN=" + partyName + ",OU=Domibus,O=eDelivery,C=EU");
        dynamicDiscoveryLookupService.saveDynamicDiscoveryLookupTime(finalRecipient, endpointUrl, partyName, partyType, Arrays.asList("myProcess"), partyName, partyCertificate);

        //we check that the final recipient URL is saved in the cache
        assertEquals(endpointUrl, dynamicDiscoveryLookupService.getFinalRecipientAccessPointUrls().get(finalRecipient));

        //we set the DDC time for the finalRecipient to 2h ago so that it's expired
        Date ddcTimeFinalRecipient = DateUtils.addHours(new Date(), 2 * -1);
        setDynamicDiscoveryTime(finalRecipient, ddcTimeFinalRecipient);

        List<DynamicDiscoveryLookupEntity> dynamicDiscoveryLookupEntities = dynamicDiscoveryLookupDao.findAll();
        assertEquals(1, dynamicDiscoveryLookupEntities.size());

        dynamicDiscoveryLookupService.deleteExpiredDdcFinalRecipients(new Date());

        dynamicDiscoveryLookupEntities = dynamicDiscoveryLookupDao.findAll();
        assertEquals(0, dynamicDiscoveryLookupEntities.size());

        //we verify that the URL for final recipient was removed from the cache
        assertFalse(dynamicDiscoveryLookupService.getFinalRecipientAccessPointUrls().containsKey(finalRecipient));
    }

    protected void setDynamicDiscoveryTime(String finalRecipient, Date date) {
        final DynamicDiscoveryLookupEntity finalRecipient1Entity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient);
        finalRecipient1Entity.setDynamicDiscoveryTime(date);
        dynamicDiscoveryLookupDao.createOrUpdate(finalRecipient1Entity);
    }
}
