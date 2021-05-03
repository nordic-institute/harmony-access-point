package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.Property;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PartPropertyDaoTestIT extends AbstractIT {

    @Autowired
    PartPropertyDao partPropertyDao;

    @Transactional
    @Test
    public void findOrCreateProperty() {
        final PartProperty applicationOctetStreamProperty = partPropertyDao.findOrCreateProperty(Property.MIME_TYPE, UserMessageDefaultFactory.APPLICATION_OCTET_STREAM, "string");
        assertNotNull(applicationOctetStreamProperty.getEntityId());
        assertEquals(UserMessageDefaultFactory.APPLICATION_OCTET_STREAM, applicationOctetStreamProperty.getValue());

        final PartProperty applicationOctetStreamPropertyFromDb = partPropertyDao.findOrCreateProperty(Property.MIME_TYPE, UserMessageDefaultFactory.APPLICATION_OCTET_STREAM, "string");
        assertNotNull(applicationOctetStreamPropertyFromDb);
        assertEquals(applicationOctetStreamProperty.getEntityId(), applicationOctetStreamPropertyFromDb.getEntityId());

        final PartProperty xmlProperty = partPropertyDao.findOrCreateProperty(Property.MIME_TYPE, UserMessageDefaultFactory.TEXT_XML, "string");
        assertEquals(UserMessageDefaultFactory.TEXT_XML, xmlProperty.getValue());
    }
}