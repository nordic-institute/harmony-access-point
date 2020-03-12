package eu.domibus.core.replication;

import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class UIMessageDiffDaoImplTest {

    @Tested
    UIMessageDiffDaoImpl uiMessageDiffDao;

    @Injectable
    EntityManager entityManager;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Test
    public void test_findAll(final @Mocked TypedQuery query) {

        //tested method
        uiMessageDiffDao.findAll();

        new FullVerifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture(), UIMessageDiffEntity.class);
            Assert.assertEquals("UIMessageDiffEntity.findDiffMessages", namedQueryActual);

            query.getResultList();
        }};
    }

    @Test
    public void test_findAllNative(final @Mocked TypedQuery query) {

        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

        }};

        //tested method
        uiMessageDiffDao.findAllNative();

        new FullVerifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture(), UIMessageDiffEntity.class);
            Assert.assertEquals("UIMessageDiffEntity.findDiffMessages_" + DataBaseEngine.ORACLE.name(), namedQueryActual);

            query.getResultList();
        }};
    }

    @Test
    public void test_countAll(final @Mocked TypedQuery<Long> query) {

        new Expectations() {{
            query.getSingleResult();
            result = 200L;
        }};

        //tested method
        uiMessageDiffDao.countAll();

        new Verifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture(), Long.class);
            Assert.assertEquals("UIMessageDiffEntity.countDiffMessages", namedQueryActual);
        }};
    }

    @Test
    public void test_countAllNative(final @Mocked Query query) {
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

            query.getSingleResult();
            result = 200L;
        }};

        //tested method
        uiMessageDiffDao.countAllNative();

        new Verifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture());
            Assert.assertEquals("UIMessageDiffEntity.countDiffMessages_" + DataBaseEngine.ORACLE.name(), namedQueryActual);
        }};
    }

    @Test
    public void test_findAll1(final @Mocked TypedQuery<UIMessageDiffEntity> query) {

        //tested method
        uiMessageDiffDao.findAll();

        new Verifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture(), UIMessageDiffEntity.class);
            Assert.assertEquals("UIMessageDiffEntity.findDiffMessages", namedQueryActual);

            query.getResultList();
        }};
    }

    @Test
    public void test_findAllNativeWithLimit(final @Mocked TypedQuery query) {
        final int limit = 20;
        new Expectations() {{
            domibusConfigurationService.getDataBaseEngine();
            result = DataBaseEngine.ORACLE;

            query.setFirstResult(0);
            query.setMaxResults(limit);

        }};

        //tested method
        uiMessageDiffDao.findAllNative(limit);

        new Verifications() {{
            String namedQueryActual;
            entityManager.createNamedQuery(namedQueryActual = withCapture(), UIMessageDiffEntity.class);
            Assert.assertEquals("UIMessageDiffEntity.findDiffMessages_" + DataBaseEngine.ORACLE.name(), namedQueryActual);

            query.getResultList();
        }};
    }
}