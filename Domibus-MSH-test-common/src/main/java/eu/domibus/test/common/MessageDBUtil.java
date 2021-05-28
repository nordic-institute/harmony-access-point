package eu.domibus.test.common;

import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author idragusa
 * @since 5.0
 */
@Component
public class MessageDBUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageDBUtil.class);

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    public Map<String, Integer> getTableCounts(List<String> tablesToExclude) {
        Map<String, Integer> rownums = new HashMap<>();
        Query query = entityManager.createNativeQuery("SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_name LIKE 'TB_%'");
        try {
            List<String> tableNames = query.getResultList();
            tableNames.stream().forEach(tableName -> rownums.put(tableName, getCounter(tableName)));
        } catch (NoResultException nrEx) {
            return null;
        }
        tablesToExclude.stream().forEach(tableName -> rownums.remove(tableName));
        return rownums;
    }

    public Integer getCounter(String tableName) {
        String selectStr = "SELECT count(*) from " + tableName;
        Query query = entityManager.createNativeQuery(selectStr);
        BigInteger counter = (BigInteger)query.getSingleResult();
        LOG.trace("Table [{}] has counter [{}]", tableName, counter);

        return counter.intValue();
    }
}