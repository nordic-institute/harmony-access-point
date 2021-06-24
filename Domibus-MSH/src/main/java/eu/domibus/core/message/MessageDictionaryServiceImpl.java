package eu.domibus.core.message;

import eu.domibus.api.model.AgreementRefEntity;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.PartyId;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;

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

    public MessageDictionaryServiceImpl(MpcDao mpcDao, ActionDao actionDao, ServiceDao serviceDao, PartyIdDao partyIdDao, AgreementDao agreementDao, MessagePropertyDao messagePropertyDao, PartPropertyDao partPropertyDao, PartyRoleDao partyRoleDao) {
        this.mpcDao = mpcDao;
        this.actionDao = actionDao;
        this.serviceDao = serviceDao;
        this.partyIdDao = partyIdDao;
        this.agreementDao = agreementDao;
        this.messagePropertyDao = messagePropertyDao;
        this.partPropertyDao = partPropertyDao;
        this.partyRoleDao = partyRoleDao;
    }

    public AgreementRefEntity findOrCreateAgreement(String value, String type) {
        AgreementRefEntity entity = agreementDao.findExistingAgreement(value, type);
        if (entity != null) {
            return entity;
        }
        try {
            return agreementDao.findOrCreateAgreement(value, type);
        } catch (PersistenceException e) {
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
        } catch (PersistenceException e) {
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
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                LOG.debug("Constraint violation when trying to insert dictionary entry, trying again (once)...");
                return partyIdDao.findOrCreateParty(value, type);
            }
            throw e;
        }
    }
}
