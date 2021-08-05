package eu.domibus.core.message;

import eu.domibus.api.model.PartInfo;
import eu.domibus.core.dao.BasicDao;
import org.hibernate.jpa.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class PartInfoDao extends BasicDao<PartInfo> {

    public PartInfoDao() {
        super(PartInfo.class);
    }

    public List<PartInfo> findPartInfoByUserMessageEntityId(final Long userMessageEntityId) {
        final Query query = this.em.createNamedQuery("PartInfo.findPartInfos");
        query.setParameter("ENTITY_ID", userMessageEntityId);
        query.setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
        return query.getResultList();
    }

    public List<String> findFileSystemPayloadFilenames(List<String> userMessageEntityIds) {
        TypedQuery<String> query = em.createNamedQuery("PartInfo.findFilenames", String.class);
        query.setParameter("MESSAGEIDS", userMessageEntityIds);
        return query.getResultList();
    }

    public void clearDatabasePayloads(final List<PartInfo> partInfos) {
        final Query emptyQuery = em.createNamedQuery("PartInfo.emptyPayloads");
        emptyQuery.setParameter("PARTINFOS", partInfos);
        emptyQuery.executeUpdate();
    }

}
