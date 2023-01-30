package eu.domibus.core.message.dictionary;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.core.message.MessageStatusDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 5.0.3
 */
@Transactional
public class StaticDictionaryServiceImplTestIT extends AbstractIT {

    @Autowired
    StaticDictionaryServiceImpl staticDictionaryService;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected NotificationStatusDao notificationStatusDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Test
    public void createEntries() {
        staticDictionaryService.createStaticDictionaryEntries();

        //check message statuses
        final List<MessageStatus> messageStatusesFromDb = messageStatusDao.findAll().stream().map(messageStatusEntity -> messageStatusEntity.getMessageStatus()).collect(Collectors.toList());
        final List<MessageStatus> messageStatuses = Arrays.asList(MessageStatus.values());
        assertEquals(messageStatuses.size(), messageStatusesFromDb.size());

        //check notification statuses
        final List<NotificationStatus> notificationStatusesFromDb = notificationStatusDao.findAll().stream().map(notificationStatusEntity -> notificationStatusEntity.getStatus()).collect(Collectors.toList());
        final List<NotificationStatus> notificationStatuses = Arrays.asList(NotificationStatus.values());
        assertEquals(notificationStatuses.size(), notificationStatusesFromDb.size());

        //check role statuses
        final List<MSHRole> rolesFromDb = mshRoleDao.findAll().stream().map(mshRoleEntity -> mshRoleEntity.getRole()).collect(Collectors.toList());
        final List<MSHRole> roles = Arrays.asList(MSHRole.values());
        assertEquals(roles.size(), rolesFromDb.size());
    }
}