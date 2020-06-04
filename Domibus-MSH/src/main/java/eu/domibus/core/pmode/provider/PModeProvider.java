
package eu.domibus.core.pmode.provider;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.Service;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.crypto.spi.PullRequestPmodeData;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.ConfigurationRawDAO;
import eu.domibus.core.pmode.validation.PModeValidationService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author Christian Koch, Stefan Mueller
 */
public abstract class PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeProvider.class);

    public static final String SCHEMAS_DIR = "schemas/";
    public static final String DOMIBUS_PMODE_XSD = "domibus-pmode.xsd";

    protected static final String OPTIONAL_AND_EMPTY = "OAE";

    @Autowired
    protected ConfigurationDAO configurationDAO;

    @Autowired
    protected ConfigurationRawDAO configurationRawDAO;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;

    @Autowired
    @Qualifier("jaxbContextConfig")
    private JAXBContext jaxbContext;

    @Autowired
    private MpcService mpcService;

    @Autowired
    protected SignalService signalService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    XMLUtil xmlUtil;

    @Autowired
    PModeValidationService pModeValidationService;

    protected abstract void init();

    public abstract void refresh();

    public abstract boolean isConfigurationLoaded();

    public byte[] getPModeFile(int id) {
        final ConfigurationRaw rawConfiguration = getRawConfiguration(id);
        return getRawConfigurationBytes(rawConfiguration);
    }

    private byte[] getRawConfigurationBytes(ConfigurationRaw rawConfiguration) {
        if (rawConfiguration != null) {
            return rawConfiguration.getXml();
        }
        return new byte[0];
    }

    public ConfigurationRaw getRawConfiguration(long id) {
        return this.configurationRawDAO.getConfigurationRaw(id);
    }

    public PModeArchiveInfo getCurrentPmode() {
        final ConfigurationRaw currentRawConfiguration = this.configurationRawDAO.getCurrentRawConfiguration();
        if (currentRawConfiguration != null) {
            return new PModeArchiveInfo(
                    currentRawConfiguration.getEntityId(),
                    currentRawConfiguration.getConfigurationDate(),
                    "",
                    currentRawConfiguration.getDescription());
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removePMode(int id) {
        LOG.debug("Removing PMode with id: [{}]", id);
        configurationRawDAO.deleteById(id);
    }

    public List<PModeArchiveInfo> getRawConfigurationList() {
        return this.configurationRawDAO.getDetailedConfigurationRaw();
    }

    public UnmarshallerResult parsePMode(byte[] bytes) throws XmlProcessingException {
        //unmarshall the PMode with whitespaces ignored
        UnmarshallerResult unmarshalledConfigurationWithWhiteSpacesIgnored = unmarshall(bytes, true);

        if (!unmarshalledConfigurationWithWhiteSpacesIgnored.isValid()) {
            String errorMessage = "The PMode file is not XSD compliant(whitespaces are ignored). Please correct the issues: [" + unmarshalledConfigurationWithWhiteSpacesIgnored.getErrorMessage() + "]";
            XmlProcessingException xmlProcessingException = new XmlProcessingException(errorMessage);
            xmlProcessingException.addErrors(unmarshalledConfigurationWithWhiteSpacesIgnored.getErrors());
            throw xmlProcessingException;
        }

        //unmarshall the PMode taking into account the whitespaces
        return unmarshall(bytes, false);
    }

    public Configuration getPModeConfiguration(byte[] bytes) throws XmlProcessingException {
        final UnmarshallerResult unmarshallerResult = parsePMode(bytes);
        return unmarshallerResult.getResult();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    public List<ValidationIssue> updatePModes(byte[] bytes, String description) throws XmlProcessingException, PModeValidationException {
        LOG.debug("Updating the PMode");
        description = validateDescriptionSize(description);

        final UnmarshallerResult unmarshalledConfiguration = parsePMode(bytes);

        List<ValidationIssue> issues = new ArrayList<>();
        if (!unmarshalledConfiguration.isValid()) {
            issues.add(createParseWarning(unmarshalledConfiguration));
        }

        Configuration configuration = unmarshalledConfiguration.getResult();

        issues.addAll(pModeValidationService.validate(configuration));

        configurationDAO.updateConfiguration(configuration);

        //save the raw configuration
        final ConfigurationRaw configurationRaw = new ConfigurationRaw();
        configurationRaw.setConfigurationDate(Calendar.getInstance().getTime());
        configurationRaw.setXml(bytes);
        configurationRaw.setDescription(description);
        configurationRawDAO.create(configurationRaw);

        LOG.info("Configuration successfully updated");
        this.refresh();

        // Sends a message into the topic queue in order to refresh all the singleton instances of the PModeProvider.
        signalService.signalPModeUpdate();

        return issues;
    }

    private ValidationIssue createParseWarning(UnmarshallerResult unmarshalledConfiguration) {
        List<String> resultMessage = new ArrayList<>();
        resultMessage.add("The PMode file is not XSD compliant. It is recommended to correct the issues:");
        resultMessage.addAll(unmarshalledConfiguration.getErrors());
        final String message = StringUtils.join(resultMessage, " ");

        LOG.warn(message);

        return new ValidationIssue(message, ValidationIssue.Level.WARNING);
    }

    private String validateDescriptionSize(final String description) {
        if (StringUtils.isNotEmpty(description) && description.length() > 255) {
            return description.substring(0, 254);
        }
        return description;
    }


    public UnmarshallerResult unmarshall(byte[] bytes, boolean ignoreWhitespaces) throws XmlProcessingException {
        Configuration configuration = null;
        UnmarshallerResult unmarshallerResult = null;

        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(SCHEMAS_DIR + DOMIBUS_PMODE_XSD);
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(bytes);

        try {
            unmarshallerResult = xmlUtil.unmarshal(ignoreWhitespaces, jaxbContext, xmlStream, xsdStream);
            configuration = unmarshallerResult.getResult();
        } catch (JAXBException | SAXException | ParserConfigurationException | XMLStreamException e) {
            LOG.error("Error unmarshalling the PMode", e);
            throw new XmlProcessingException("Error unmarshalling the PMode: " + e.getMessage(), e);
        }
        if (configuration == null) {
            throw new XmlProcessingException("Error unmarshalling the PMode: could not process the PMode file");
        }
        return unmarshallerResult;
    }

    public byte[] serializePModeConfiguration(Configuration configuration) throws XmlProcessingException {

        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(SCHEMAS_DIR + DOMIBUS_PMODE_XSD);

        byte[] serializedPMode;
        try {
            serializedPMode = xmlUtil.marshal(jaxbContext, configuration, xsdStream);
        } catch (JAXBException | SAXException | ParserConfigurationException | XMLStreamException e) {
            LOG.error("Error marshalling the PMode", e);
            throw new XmlProcessingException("Error marshalling the PMode: " + e.getMessage(), e);
        }
        return serializedPMode;
    }

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_FROM, DomibusLogger.MDC_TO, DomibusLogger.MDC_SERVICE, DomibusLogger.MDC_ACTION})
    public MessageExchangeConfiguration findUserMessageExchangeContext(final UserMessage userMessage, final MSHRole mshRole, final boolean isPull) throws EbMS3Exception {

        final String agreementName;
        final String service;
        final String action;
        final String leg;
        String mpc;
        String senderParty;
        String receiverParty = StringUtils.EMPTY;
        final Set<PartyId> fromPartyId = userMessage.getPartyInfo().getFrom().getPartyId();
        final Set<PartyId> toPartyId = userMessage.getPartyInfo().getTo().getPartyId();
        final String messageId = userMessage.getMessageInfo().getMessageId();
        //add messageId to MDC map
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.putMDC(DomibusLogger.MDC_FROM, userMessage.getFromFirstPartyId());
        LOG.putMDC(DomibusLogger.MDC_TO, userMessage.getToFirstPartyId());
        LOG.putMDC(DomibusLogger.MDC_SERVICE, userMessage.getCollaborationInfo().getService().getValue());
        LOG.putMDC(DomibusLogger.MDC_ACTION, userMessage.getCollaborationInfo().getAction());

        try {
            agreementName = findAgreement(userMessage.getCollaborationInfo().getAgreementRef());
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_AGREEMENT_FOUND, agreementName, userMessage.getCollaborationInfo().getAgreementRef());
            try {
                senderParty = findPartyName(fromPartyId);
                LOG.businessInfo(DomibusMessageCode.BUS_SENDER_PARTY_ID_FOUND, senderParty, fromPartyId);
            } catch (EbMS3Exception exc) {
                LOG.businessError(DomibusMessageCode.BUS_SENDER_PARTY_ID_NOT_FOUND, fromPartyId);
                exc.setErrorDetail("Sender party could not found for the value  " + fromPartyId);
                exc.setMshRole(mshRole);
                throw exc;
            }
            try {
                receiverParty = findPartyName(toPartyId);
                LOG.businessInfo(DomibusMessageCode.BUS_RECEIVER_PARTY_ID_FOUND, receiverParty, toPartyId);
            } catch (EbMS3Exception exc) {
                if (isPull && mpcService.forcePullOnMpc(userMessage)) {
                    LOG.info("Receiver party not found in pMode, extract from MPC");
                    receiverParty = mpcService.extractInitiator(userMessage.getMpc());
                    exc.setErrorDetail("Receiver Party extracted from MPC is " + receiverParty + ", and SenderParty is " + senderParty);
                } else {
                    LOG.businessError(DomibusMessageCode.BUS_RECEIVER_PARTY_ID_NOT_FOUND, toPartyId);
                    exc.setErrorDetail((receiverParty.isEmpty()) ? "Receiver party could not found for the value " + toPartyId : "Receiver Party in Pmode is " + receiverParty + ", and SenderParty is " + senderParty);
                    exc.setMshRole(mshRole);
                    throw exc;
                }
            }
            LOG.debug("Found SenderParty as [{}] and  Receiver Party as [{}]", senderParty, receiverParty);
            service = findServiceName(userMessage.getCollaborationInfo().getService());
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SERVICE_FOUND, service, userMessage.getCollaborationInfo().getService());
            action = findActionName(userMessage.getCollaborationInfo().getAction());
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_ACTION_FOUND, action, userMessage.getCollaborationInfo().getAction());
            if (isPull && mpcService.forcePullOnMpc(userMessage.getMpc())) {
                mpc = mpcService.extractBaseMpc(userMessage.getMpc());
                leg = findPullLegName(agreementName, senderParty, receiverParty, service, action, mpc);
            } else {
                mpc = userMessage.getMpc();
                leg = findLegName(agreementName, senderParty, receiverParty, service, action);
            }
            LOG.businessInfo(DomibusMessageCode.BUS_LEG_NAME_FOUND, leg, agreementName, senderParty, receiverParty, service, action, mpc);

            if ((StringUtils.equalsIgnoreCase(action, Ebms3Constants.TEST_ACTION) && (!StringUtils.equalsIgnoreCase(service, Ebms3Constants.TEST_SERVICE)))) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "ebMS3 Test Service: " + Ebms3Constants.TEST_SERVICE + " and ebMS3 Test Action: " + Ebms3Constants.TEST_ACTION + " can only be used together [CORE]", messageId, null);
            }

            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(agreementName, senderParty, receiverParty, service, action, leg, mpc);
            LOG.debug("Found pmodeKey [{}] for message [{}]", messageExchangeConfiguration.getPmodeKey(), userMessage);
            return messageExchangeConfiguration;
        } catch (EbMS3Exception e) {
            e.setRefToMessageId(messageId);
            throw e;
        } catch (IllegalStateException ise) {
            // It can happen if DB is clean and no pmodes are configured yet!
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "PMode could not be found. Are PModes configured in the database?", messageId, ise);
        }
    }

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public MessageExchangeConfiguration findUserMessageExchangeContext(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        return findUserMessageExchangeContext(userMessage, mshRole, false);
    }


    class ReloadPmodeMessageCreator implements MessageCreator {
        @Override
        public Message createMessage(Session session) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty(Command.COMMAND, Command.RELOAD_PMODE);
            m.setStringProperty(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode());
            return m;
        }
    }

    public abstract List<String> getMpcList();

    public abstract List<String> getMpcURIList();

    public abstract String findMpcUri(final String mpcName) throws EbMS3Exception;

    public abstract String findLegName(String agreementRef, String senderParty, String receiverParty, String service, String action) throws EbMS3Exception;

    public abstract String findPullLegName(String agreementRef, String senderParty, String receiverParty, String service, String action, String mpc) throws EbMS3Exception;

    public abstract String findActionName(String action) throws EbMS3Exception;

    public abstract Mpc findMpc(final String mpcValue) throws EbMS3Exception;

    public abstract String findServiceName(eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception;

    public abstract String findPartyName(Collection<PartyId> partyId) throws EbMS3Exception;

    public abstract String findAgreement(AgreementRef agreementRef) throws EbMS3Exception;

    public UserMessagePmodeData getUserMessagePmodeData(UserMessage userMessage) throws EbMS3Exception {
        final String actionValue = userMessage.getCollaborationInfo().getAction();
        final String actionName = findActionName(actionValue);
        final eu.domibus.ebms3.common.model.Service service = userMessage.getCollaborationInfo().getService();
        final String serviceName = findServiceName(service);
        final String partyName = findPartyName(userMessage.getPartyInfo().getFrom().getPartyId());
        return new UserMessagePmodeData(serviceName, actionName, partyName);
    }

    public PullRequestPmodeData getPullRequestMapping(PullRequest pullRequest) throws EbMS3Exception {
        Mpc mpc;
        try {
            LOG.debug("Find the mpc based on the pullRequest mpc [{}]", pullRequest.getMpc());
            mpc = findMpc(pullRequest.getMpc());
        } catch (EbMS3Exception e) {
            LOG.debug("Could not find the mpc [{}], check if base mpc should be used", pullRequest.getMpc());
            if (mpcService.forcePullOnMpc(pullRequest.getMpc())) {
                String mpcQualifiedName = mpcService.extractBaseMpc(pullRequest.getMpc());
                LOG.debug("Trying base mpc [{}]", mpcQualifiedName);
                mpc = findMpc(mpcQualifiedName);
            } else {
                LOG.debug("Base mpc is not to be used, rethrowing the exception", e);
                throw e;
            }
        }
        return new PullRequestPmodeData(mpc.getName());
    }

    public abstract Party getGatewayParty();

    public abstract Party getPartyByIdentifier(String partyIdentifier);

    public abstract Party getSenderParty(String pModeKey);

    public abstract Party getReceiverParty(String pModeKey);

    public abstract Service getService(String pModeKey);

    public abstract Action getAction(String pModeKey);

    public abstract Agreement getAgreement(String pModeKey);

    public abstract LegConfiguration getLegConfiguration(String pModeKey);

    public abstract boolean isMpcExistant(String mpc);

    public abstract int getRetentionDownloadedByMpcName(String mpcName);

    public abstract int getRetentionDownloadedByMpcURI(final String mpcURI);

    public abstract int getRetentionUndownloadedByMpcName(String mpcName);

    public abstract int getRetentionUndownloadedByMpcURI(final String mpcURI);

    public abstract Role getBusinessProcessRole(String roleValue);

    public String getSenderPartyNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[0];
    }

    public String getReceiverPartyNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[1];
    }

    public String getServiceNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[2];
    }

    public String getActionNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[3];
    }

    public String getAgreementRefNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[4];
    }

    public String getLegConfigurationNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(MessageExchangeConfiguration.PMODEKEY_SEPARATOR)[5];
    }

    public abstract List<Process> findPullProcessesByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration);

    public abstract List<Process> findPullProcessesByInitiator(final Party party);

    public abstract List<Process> findPullProcessByMpc(final String mpc);

    public abstract List<Process> findAllProcesses();

    public abstract List<Party> findAllParties();

    public abstract List<String> findPartyIdByServiceAndAction(final String service, final String action, final List<MessageExchangePattern> meps);

    public abstract String getPartyIdType(String partyIdentifier);

    public abstract String getServiceType(String serviceValue);

    public abstract String getRole(String roleType, String serviceValue);

    public abstract String getAgreementRef(String serviceValue);

}
