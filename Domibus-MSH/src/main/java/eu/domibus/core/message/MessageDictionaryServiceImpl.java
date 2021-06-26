package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Arrays;
import java.util.concurrent.Callable;

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
        Callable<AgreementRefEntity> findTask = () -> agreementDao.findExistingAgreement(value, type);
        Callable<AgreementRefEntity> findOrCreateTask = () -> agreementDao.findOrCreateAgreement(value, type);
        String entityDescription = "AgreementRefEntity value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public PartProperty findOrCreatePartProperty(final String name, String value, String type) {
        Callable<PartProperty> findTask = () -> partPropertyDao.findExistingProperty(name, value, type);
        Callable<PartProperty> findOrCreateTask = () -> partPropertyDao.findOrCreateProperty(name, value, type);
        String entityDescription = "PartProperty name=[" + name + "] value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public PartyId findOrCreateParty(String value, String type) {
        Callable<PartyId> findTask = () -> partyIdDao.findPartyByValueAndType(value, type); // TODO: EDELIVERY-8280
        Callable<PartyId> findOrCreateTask = () -> partyIdDao.findOrCreateParty(value, type);
        String entityDescription = "PartyId value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public PartyRole findOrCreateRole(String value) {
        Callable<PartyRole> findTask = () -> partyRoleDao.findRoleByValue(value);
        Callable<PartyRole> findOrCreateTask = () -> partyRoleDao.findOrCreateRole(value);
        String entityDescription = "PartyRole value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public ActionEntity findOrCreateAction(String value) {
        Callable<ActionEntity> findTask = () -> actionDao.findByValue(value);
        Callable<ActionEntity> findOrCreateTask = () -> actionDao.findOrCreateAction(value);
        String entityDescription = "ActionEntity value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public ServiceEntity findOrCreateService(String value, String type) {
        Callable<ServiceEntity> findTask = () -> serviceDao.findExistingService(value, type);
        Callable<ServiceEntity> findOrCreateTask = () -> serviceDao.findOrCreateService(value, type);
        String entityDescription = "ServiceEntity value=[" + value + "] type=[" + type + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    public MpcEntity findOrCreateMpc(String value) {
        Callable<MpcEntity> findTask = () -> mpcDao.findMpc(value);
        Callable<MpcEntity> findOrCreateTask = () -> mpcDao.findOrCreateMpc(value);
        String entityDescription = "MpcEntity value=[" + value + "]";

        return this.findOrCreateEntity(findTask, findOrCreateTask, entityDescription);
    }

    protected <T> T findOrCreateEntity(Callable<T> findTask, Callable<T> findOrCreateTask, String entityDescription) {
        try {
            T entity = findTask.call();
            if (entity != null) {
                return entity;
            }
            try {
                return findOrCreateTask.call();
            } catch (PersistenceException | DataIntegrityViolationException e) {
                if (e.getCause() instanceof ConstraintViolationException) {
                    LOG.info("Constraint violation when trying to insert dictionary entry [{}], trying again (once)...", entityDescription);
                    return findOrCreateTask.call();
                }
                LOG.info("Exception of type [{}] when trying to insert dictionary entry [{}], rethrowing...", e.getClass().getName(), entityDescription);
                throw e;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not find or create dictionary entry " + entityDescription, ex);
        }
    }
}
