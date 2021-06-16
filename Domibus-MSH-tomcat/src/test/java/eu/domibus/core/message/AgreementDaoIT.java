package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.AgreementRefEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Transactional
public class AgreementDaoIT extends AbstractIT {

    @Autowired
    private AgreementDao agreementDao;

    @Test
    public void testFindByNameValueAndType() {
        final String value = "value1";
        final String type = "string";
        AgreementRefEntity entity = buildEntity(value, type);
        agreementDao.create(entity);

        final AgreementRefEntity foundAgreement = agreementDao.findOrCreateAgreement(value, type);
        assertNotNull(foundAgreement);

        Assert.assertEquals(entity.getEntityId(), foundAgreement.getEntityId());
        Assert.assertEquals(value, foundAgreement.getValue());
        Assert.assertEquals(type, foundAgreement.getType());

        final AgreementRefEntity foundProperty1 = agreementDao.findOrCreateAgreement(value, type);

        Assert.assertEquals(foundAgreement.getEntityId(), foundProperty1.getEntityId());
    }

    @Test
    public void testFindByNameAndValue() {
        final String value = "value1";
        final String type = null;
        AgreementRefEntity property = buildEntity(value, type);
        agreementDao.create(property);

        final AgreementRefEntity foundProperty = agreementDao.findOrCreateAgreement(value, type);
        assertNotNull(foundProperty);

        Assert.assertEquals(property.getEntityId(), foundProperty.getEntityId());
        Assert.assertEquals(value, foundProperty.getValue());
        Assert.assertEquals(type, foundProperty.getType());

        final AgreementRefEntity foundProperty1 = agreementDao.findOrCreateAgreement(value, type);

        Assert.assertEquals(foundProperty.getEntityId(), foundProperty1.getEntityId());
    }

    private AgreementRefEntity buildEntity(String value, String type) {
        AgreementRefEntity property = new AgreementRefEntity();
        property.setValue(value);
        property.setType(type);
        return property;
    }
}