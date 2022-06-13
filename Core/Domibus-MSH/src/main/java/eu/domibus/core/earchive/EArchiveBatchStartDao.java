package eu.domibus.core.earchive;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchStartDao extends BasicDao<EArchiveBatchStart> {

    public EArchiveBatchStartDao() {
        super(EArchiveBatchStart.class);
    }

}
