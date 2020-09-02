package eu.domibus.core.message;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.pull.MessagePullDto;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author idragusa
 * @since 4.2
 */

@Repository
public class MessageInfoDao extends BasicDao<Messaging> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceImpl.class);

    public MessageInfoDao() {
        super(Messaging.class);
    }

    public List<String> findUserMessageIds(List<String> userMessageIds) {
        final TypedQuery<String> query = em.createNamedQuery("MessageInfo.findUserMessageIds", String.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        List<String> messageIds = query.getResultList();
        LOG.info("Found ids [{}]", messageIds);
        return messageIds;
    }

    public List<String> findSignalMessageIds(List<String> userMessageIds) {
        final TypedQuery<String> query = em.createNamedQuery("MessageInfo.findSignalMessageIds", String.class);
        query.setParameter("MESSAGEIDS", userMessageIds);
        List<String> messageIds = query.getResultList();
        LOG.info("Found ids [{}]", messageIds);
        return messageIds;
    }

    public int deleteMessages(List<String> messageIds) {
        final Query deleteQuery = em.createNamedQuery("MessageInfo.deleteMessages");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result  = deleteQuery.executeUpdate();
        LOG.info("deleteMessages result [{}]", result);
        return result;
    }
}

