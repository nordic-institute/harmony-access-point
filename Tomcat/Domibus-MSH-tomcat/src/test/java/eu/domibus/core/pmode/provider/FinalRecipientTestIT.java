package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.participant.FinalRecipientEntity;
import eu.domibus.core.participant.FinalRecipientDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class FinalRecipientTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FinalRecipientTestIT.class);

    @Autowired
    protected FinalRecipientDao finalRecipientDao;

    @Autowired
    protected FinalRecipientService finalRecipientService;

    @After
    public void clean() {
        deleteAllEntriesFromDB();
    }

    protected void deleteAllEntriesFromDB() {
        //we delete all data from DB to check if the URL is retrieved from the cache
        finalRecipientDao.deleteAll(finalRecipientDao.findAll());
    }

    @Transactional
    @Test
    public void saveFinalRecipientEndpoint() {
        final String finalRecipient = "0208:111";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        finalRecipientService.saveFinalRecipientEndpoint(finalRecipient, endpointUrl);

        final List<FinalRecipientEntity> finalRecipientEntities = finalRecipientDao.findAll();
        assertEquals(1, finalRecipientEntities.size());

        //we clear the cache
        finalRecipientService.clearFinalRecipientAccessPointUrlsCache();

        //the final recipient URL should be retrieved from database and added in the cache
        assertEquals(endpointUrl, finalRecipientService.getEndpointURL(finalRecipient));

        //we delete all data from DB to check if the URL is retrieved from the cache
        deleteAllEntriesFromDB();

        //the final recipient URL should be retrieved from the cache
        assertEquals(endpointUrl, finalRecipientService.getEndpointURL(finalRecipient));
    }

    @Test
    public void testUniqueConstraintViolation() {
        final String finalRecipient = "0208:111";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        final FinalRecipientEntity finalRecipientEntity1 = new FinalRecipientEntity();
        finalRecipientEntity1.setFinalRecipient(finalRecipient);
        finalRecipientEntity1.setEndpointURL(endpointUrl);

        finalRecipientDao.createOrUpdate(finalRecipientEntity1);

        final FinalRecipientEntity finalRecipientEntity2 = new FinalRecipientEntity();
        finalRecipientEntity2.setFinalRecipient(finalRecipient);
        finalRecipientEntity2.setEndpointURL(endpointUrl);

        try {
            finalRecipientDao.createOrUpdate(finalRecipientEntity2);
            fail("It should have triggered a unique constraint violation");
        } catch (DataIntegrityViolationException e) {
            //in a cluster environment, an entity associated for a final recipient can be created in parallel and a unique constraint is raised
            //in case a constraint violation occurs we don't do anything because the other node added the latest data in parallel
            LOG.warn("Could not create or update final recipient entity with entity id [{}]. It could be that another node updated the same entity in parallel", finalRecipientEntity2.getEntityId(), e);
        }
    }


    @Transactional
    @Test
    public void deleteFinalRecipients() {
        final String finalRecipient = "0208:111";
        final String endpointUrl = "http://localhost/domibus/services/msh";
        finalRecipientService.saveFinalRecipientEndpoint(finalRecipient, endpointUrl);

        List<FinalRecipientEntity> finalRecipientEntities = finalRecipientDao.findAll();
        assertEquals(1, finalRecipientEntities.size());

        finalRecipientService.deleteFinalRecipients(finalRecipientEntities);

        finalRecipientEntities = finalRecipientDao.findAll();
        assertEquals(0, finalRecipientEntities.size());
    }
}
