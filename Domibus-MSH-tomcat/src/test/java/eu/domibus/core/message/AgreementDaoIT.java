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
    public void testFindByValueAndType() {
        final String value = "value1";
        final String type = "string";
        AgreementRefEntity entity = buildEntity(value, type);
        agreementDao.create(entity);

        final AgreementRefEntity foundAgreement = agreementDao.findOrCreateAgreement(value, type);
        assertNotNull(foundAgreement);

        Assert.assertEquals(entity.getEntityId(), foundAgreement.getEntityId());
        Assert.assertEquals(value, foundAgreement.getValue());
        Assert.assertEquals(type, foundAgreement.getType());

        final AgreementRefEntity foundAgreement1 = agreementDao.findOrCreateAgreement(value, type);

        Assert.assertEquals(foundAgreement.getEntityId(), foundAgreement1.getEntityId());
    }

    @Test
    public void testFindByValue() {
        final String value = "value1";
        final String type = null;
        AgreementRefEntity entity = buildEntity(value, type);
        agreementDao.create(entity);

        final AgreementRefEntity foundAgreement = agreementDao.findOrCreateAgreement(value, type);
        assertNotNull(foundAgreement);

        Assert.assertEquals(entity.getEntityId(), foundAgreement.getEntityId());
        Assert.assertEquals(value, foundAgreement.getValue());
        Assert.assertEquals(type, foundAgreement.getType());

        final AgreementRefEntity foundAgreement1 = agreementDao.findOrCreateAgreement(value, type);

        Assert.assertEquals(foundAgreement.getEntityId(), foundAgreement1.getEntityId());
    }

    @Test
    public void testFindOrCreate() {
        final String value = "value1";

        final AgreementRefEntity foundAgreement1 = agreementDao.findOrCreateAgreement(value, null);
        assertNotNull(foundAgreement1);

        final AgreementRefEntity foundAgreement2 = agreementDao.findOrCreateAgreement(value, "");
        assertNotNull(foundAgreement2);

        final AgreementRefEntity foundAgreement3 = agreementDao.findOrCreateAgreement(value, "type1");
        assertNotNull(foundAgreement3);

        Assert.assertEquals(foundAgreement1.getEntityId(), foundAgreement2.getEntityId());
        Assert.assertNotEquals(foundAgreement1.getEntityId(), foundAgreement3.getEntityId());
    }

    private AgreementRefEntity buildEntity(String value, String type) {
        AgreementRefEntity property = new AgreementRefEntity();
        property.setValue(value);
        property.setType(type);
        return property;
    }
}