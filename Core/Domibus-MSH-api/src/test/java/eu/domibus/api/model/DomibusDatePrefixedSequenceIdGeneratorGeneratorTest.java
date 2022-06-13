package eu.domibus.api.model;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class DomibusDatePrefixedSequenceIdGeneratorGeneratorTest {
    @Parameterized.Parameters(name = "{index}: {0}")
    // test desc. result local date, sequence
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"Base Integer sequence generator", new Long("210809150000000050"), LocalDateTime.parse("2021-08-09T15:15:30"), Integer.valueOf(50)},
                {"Base Long sequence generator ", new Long("210809150000000050"), LocalDateTime.parse("2021-08-09T15:15:30"), Long.valueOf(50)},
                {"Base BigInteger sequence generator ", new Long("210809150000000050"), LocalDateTime.parse("2021-08-09T15:15:30"), BigInteger.valueOf(50)},
                {"Base BigInteger change hour", new Long("210809170000000050"), LocalDateTime.parse("2021-08-09T17:15:30"), BigInteger.valueOf(50)},
                {"Base BigInteger change sequence", new Long("210809150000013123"), LocalDateTime.parse("2021-08-09T15:15:30"), BigInteger.valueOf(13123)},
                {"Base BigInteger change date", new Long("200101150000013123"), LocalDateTime.parse("2020-01-01T15:15:30"), BigInteger.valueOf(13123)},

        });
    }

    DomibusDatePrefixedSequenceIdGeneratorGenerator testInstance = Mockito.spy(new DomibusDatePrefixedSequenceIdGeneratorGenerator() {
        @Override
        public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {}

        @Override
        public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
            return null;
        }

        @Override
        public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
            return null;
        }
        @Override
        public Object generatorKey() {
            return null;
        }

        @Override
        public void registerExportables(Database database) {
        }

        @Override
        public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
            return null;
        }
    });


    String testName;
    Long result;
    LocalDateTime currentDate;
    Serializable generatedSequenceObject;

    public DomibusDatePrefixedSequenceIdGeneratorGeneratorTest(String testName, Long result, LocalDateTime currentDate, Serializable generatedSequenceObject) {
        this.testName = testName;
        this.result = result;
        this.currentDate = currentDate;
        this.generatedSequenceObject = generatedSequenceObject;
    }

    @Test
    public void generateDomibus() {
        // given
        Mockito.when(testInstance.generate(Matchers.anyObject(), Matchers.anyObject())).thenReturn(generatedSequenceObject);
        Mockito.when(testInstance.getCurrentDate()).thenReturn(currentDate);
        // when
        Serializable sequence = testInstance.generateDomibus(Matchers.anyObject(), Matchers.anyObject());
        //then
        assertEquals(result, sequence);
    }
}