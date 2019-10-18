package eu.domibus.plugin.webService.dao;

import eu.domibus.plugin.webService.model.MessageInfoEntity;
import org.springframework.stereotype.Repository;

/**
 * Simple MessageInfo class to exemplify the use of the Domibus EntityManager
 *
 * @author idragusa
 * @since 4.2
 */
@Repository
public class MessageInfoDao extends BasicDao<MessageInfoEntity> {

    public MessageInfoDao() {
        super(MessageInfoEntity.class);
    }

}
