package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Arrays;

@Service
public class MessageDictionaryServiceImpl implements MessageDictionaryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageDictionaryServiceImpl.class);

    protected MpcDao mpcDao;
    protected ActionDao actionDao;
    protected ServiceDao serviceDao;
    protected PartyIdDao partyIdDao;
    protected AgreementDao agreementDao;
    protected MessagePropertyDao messagePropertyDao;
    protected PartPropertyDao partPropertyDao;
    protected PartyRoleDao partyRoleDao;
    protected MessageStatusDao messageStatusDao;
    protected NotificationStatusDao notificationStatusDao;
    protected MshRoleDao mshRoleDao;

    public MessageDictionaryServiceImpl(MpcDao mpcDao, ActionDao actionDao, ServiceDao serviceDao, PartyIdDao partyIdDao, AgreementDao agreementDao, MessagePropertyDao messagePropertyDao, PartPropertyDao partPropertyDao, PartyRoleDao partyRoleDao, MessageStatusDao messageStatusDao, NotificationStatusDao notificationStatusDao, MshRoleDao mshRoleDao) {
        this.mpcDao = mpcDao;
        this.actionDao = actionDao;
        this.serviceDao = serviceDao;
        this.partyIdDao = partyIdDao;
        this.agreementDao = agreementDao;
        this.messagePropertyDao = messagePropertyDao;
        this.partPropertyDao = partPropertyDao;
        this.partyRoleDao = partyRoleDao;
        this.messageStatusDao = messageStatusDao;
        this.notificationStatusDao = notificationStatusDao;
        this.mshRoleDao = mshRoleDao;
    }

    @Transactional
    public void createStaticDictionaryEntries() {
        Arrays.stream(MessageStatus.values()).forEach(messageStatus -> messageStatusDao.findOrCreate(messageStatus));
        Arrays.stream(NotificationStatus.values()).forEach(notificationStatus -> notificationStatusDao.findOrCreate(notificationStatus));
        Arrays.stream(MSHRole.values()).forEach(mshRole -> mshRoleDao.findOrCreate(mshRole));
    }

    public AgreementRefEntity findOrCreateAgreement(String value, String type) {
        AgreementRefEntity entity = agreementDao.findExistingAgreement(value, type);
        if (entity != null) {
            return entity;
        }
        try {
            return agreementDao.findOrCreateAgreement(value, type);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return agreementDao.findOrCreateAgreement(value, type);
            }
            throw e;
        }
    }

    public PartProperty findOrCreatePartProperty(final String name, String value, String type) {
        PartProperty entity = partPropertyDao.findExistingProperty(name, value, type);
        if (entity != null) {
            return entity;
        }
        try {
            return partPropertyDao.findOrCreateProperty(name, value, type);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return partPropertyDao.findOrCreateProperty(name, value, type);
            }
            throw e;
        }
    }

    public PartyId findOrCreateParty(String value, String type) {
        PartyId entity = partyIdDao.findPartyByValueAndType(value, type);
        if (entity != null) {
            return entity;
        }
        try {
            return partyIdDao.findOrCreateParty(value, type);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return partyIdDao.findOrCreateParty(value, type);
            }
            throw e;
        }
    }

    public PartyRole findOrCreateRole(String value) {
        PartyRole entity = partyRoleDao.findRoleByValue(value);
        if (entity != null) {
            return entity;
        }
        try {
            return partyRoleDao.findOrCreateRole(value);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return partyRoleDao.findOrCreateRole(value);
            }
            throw e;
        }
    }

    public ActionEntity findOrCreateAction(String value) {
        ActionEntity entity = actionDao.findByValue(value);
        if (entity != null) {
            return entity;
        }
        try {
            return actionDao.findOrCreateAction(value);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return actionDao.findOrCreateAction(value);
            }
            throw e;
        }
    }

    public ServiceEntity findOrCreateService(String value, String type) {
        ServiceEntity entity = serviceDao.findExistingService(value, type);
        if (entity != null) {
            return entity;
        }
        try {
            return serviceDao.findOrCreateService(value, type);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return serviceDao.findOrCreateService(value, type);
            }
            throw e;
        }
    }

    public MpcEntity findOrCreateMpc(String value) {
        MpcEntity entity = mpcDao.findMpc(value);
        if (entity != null) {
            return entity;
        }
        try {
            return mpcDao.findOrCreateMpc(value);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return mpcDao.findOrCreateMpc(value);
            }
            throw e;
        }
    }
}
