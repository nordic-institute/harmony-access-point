package eu.domibus.core.message;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.resend.MessageResendEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@Repository
public class UserMessageRestoreDao extends BasicDao<MessageResendEntity> {

    private static final String MESSAGE_ID = "MESSAGE_ID";

    public UserMessageRestoreDao() {
        super(MessageResendEntity.class);
    }

    public List<String>  findAllMessagesToRestore(){
        return em.createNamedQuery("MessageResendEntity.findAllMessageIds", String.class).getResultList();
    }

  /*  public String  findByMessageIdToRestore(final String messageId){
        Query query = em.createNamedQuery("MessageResendEntity.findByMessageId");
        query.setParameter(MESSAGE_ID, messageId);
        return (String) query.getSingleResult();
    }*/

    @Transactional
    public void delete(final String messageId) {
        Query query = em.createNamedQuery("MessageResendEntity.delete");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }
}
