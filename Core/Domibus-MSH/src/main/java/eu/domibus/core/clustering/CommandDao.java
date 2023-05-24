package eu.domibus.core.clustering;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Repository
public class CommandDao extends BasicDao<CommandEntity> {

    public CommandDao() {
        super(CommandEntity.class);
    }

    public List<CommandEntity> findCommandsByServerName(String serverName) {
        final TypedQuery<CommandEntity> namedQuery = em.createNamedQuery("CommandEntity.findByServerName", CommandEntity.class);
        namedQuery.setParameter("SERVER_NAME", serverName);
        return namedQuery.getResultList();
    }
}
