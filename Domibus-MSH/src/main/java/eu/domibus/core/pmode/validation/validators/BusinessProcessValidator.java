package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ion Perpegel
 * @author Catalin Enache
 * @since 4.2
 * <p>
 * Validates that all processes of a pMode have Agreement, mep,binding, etc
 */
@Component
@Order(1)
public class BusinessProcessValidator implements PModeValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BusinessProcessValidator.class);

    final PModeValidationHelper pModeValidationHelper;

    public BusinessProcessValidator(PModeValidationHelper pModeValidationHelper) {
        this.pModeValidationHelper = pModeValidationHelper;
    }

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getProcesses()
                .forEach(process -> performValidations(issues, process, pMode.getBusinessProcesses().getPartyIdTypes()));
        return issues;
    }

    protected void performValidations(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes) {
        //agreement
        validateAgreement(issues, process);

        //mep
        validateMep(issues, process);

        //binding
        validateBinding(issues, process);

        //initiator Role
        validateInitiatorRole(issues, process);

        //responder Role
        validateResponderRole(issues, process);

        //initiator Parties
        validateInitiatorParties(issues, process, partyIdTypes);

        //responder Parties
        validateResponderParties(issues, process, partyIdTypes);

        //leg configuration
        validateLegConfiguration(issues, process);
    }

    protected void validateAgreement(List<ValidationIssue> issues, Process process) {
        if (process.getAgreement() != null) {
            LOG.trace("process.getAgreement() is not null");
            return;
        }
        String name = pModeValidationHelper.getAttributeValue(process, "agreementXml", String.class);
        //agreements can be null
        if (StringUtils.isNotEmpty(name)) {
            createIssue(issues, process, name, "Agreement [%s] of process [%s] not found in business process agreements.");
        }
    }

    protected void validateMep(List<ValidationIssue> issues, Process process) {
        if (process.getMep() != null) {
            LOG.trace("process.getMep() is not null");
            return;
        }
        String name = pModeValidationHelper.getAttributeValue(process, "mepXml", String.class);
        createIssue(issues, process, name, "Mep [%s] of process [%s] not found in business process meps.");
    }

    protected void validateBinding(List<ValidationIssue> issues, Process process) {
        if (process.getMepBinding() != null) {
            LOG.trace("process.getMepBinding() is not null");
            return;
        }
        String name = pModeValidationHelper.getAttributeValue(process, "bindingXml", String.class);
        createIssue(issues, process, name, "Mep binding [%s] of process [%s] not found in business process bindings.");
    }

    protected void validateInitiatorRole(List<ValidationIssue> issues, Process process) {
        if (process.getInitiatorRole() != null) {
            LOG.trace("process.getInitiatorRole() is not null");
            return;
        }
        String name = pModeValidationHelper.getAttributeValue(process, "initiatorRoleXml", String.class);
        createIssue(issues, process, name, "Initiator role [%s] of process [%s] not found in business process roles.");
    }

    protected void validateResponderRole(List<ValidationIssue> issues, Process process) {
        if (process.getResponderRole() != null) {
            LOG.trace("process.getResponderRole() is not null");
            return;
        }
        String name = pModeValidationHelper.getAttributeValue(process, "responderRoleXml", String.class);
        createIssue(issues, process, name, "Responder role [%s] of process [%s] not found in business process roles.");
    }


    protected void validateInitiatorParties(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes) {
        Set<Party> validInitiatorParties = process.getInitiatorParties();
        InitiatorParties initiatorPartiesXml = process.getInitiatorPartiesXml();
        if (initiatorPartiesXml == null) {
            LOG.trace("initiatorPartiesXml is null, exiting");
            return;
        }
        List<InitiatorParty> allInitiatorParties = initiatorPartiesXml.getInitiatorParty();

        if (!CollectionUtils.isEmpty(allInitiatorParties)) {
            allInitiatorParties.stream()
                    .filter(initiatorParty -> validInitiatorParties.stream().noneMatch(validInitiatorParty -> validInitiatorParty.getName().equalsIgnoreCase(initiatorParty.getName())))
                    .forEach(party -> createIssue(issues, process, party.getName(), "Initiator party [%s] of process [%s] not found in business process parties"));
        }
        validateInitiatorPartyIdType(issues, process, partyIdTypes, validInitiatorParties);
    }

    protected void validateInitiatorPartyIdType(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes, Set<Party> validInitiatorParties) {
        if (CollectionUtils.isEmpty(validInitiatorParties)) {
            LOG.trace("validInitiatorParties is empty or null, exiting");
            return;
        }
        validInitiatorParties.forEach(party -> checkPartyIdentifiers(issues, process, partyIdTypes, party, "Initiator Party's [%s] partyIdType of process [%s] not found in business process partyId types"));
    }

    protected void checkPartyIdentifiers(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes, Party party, String message) {
        party.getIdentifiers().forEach(identifier -> {
            if (!partyIdTypes.contains(identifier.getPartyIdType())) {
                createIssue(issues, process, party.getName(), message);
            }
        });
    }

    protected void validateResponderParties(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes) {
        Set<Party> validResponderParties = process.getResponderParties();
        ResponderParties responderPartiesXml = process.getResponderPartiesXml();
        if (responderPartiesXml == null) {
            LOG.trace("responderPartiesXml is null, exiting");
            return;
        }
        List<ResponderParty> allResponderParties = responderPartiesXml.getResponderParty();

        if (!CollectionUtils.isEmpty(allResponderParties)) {
            allResponderParties.stream()
                    .filter(responderParty -> validResponderParties.stream().noneMatch(validResponderParty -> validResponderParty.getName().equalsIgnoreCase(responderParty.getName())))
                    .forEach(party -> createIssue(issues, process, party.getName(), "Responder party [%s] of process [%s] not found in business process parties"));
        }
        validateResponderPartyIdType(issues, process, partyIdTypes, validResponderParties);
    }

    protected void validateResponderPartyIdType(List<ValidationIssue> issues, Process process, Set<PartyIdType> partyIdTypes, Set<Party> validResponderParties) {
        if (CollectionUtils.isEmpty(validResponderParties)) {
            LOG.trace("validResponderParties is empty or null, exiting");
            return;
        }
        validResponderParties.forEach(party -> checkPartyIdentifiers(issues, process, partyIdTypes, party, "Responder Party's [%s] partyIdType of process [%s] not found in business process partyId types"));
    }

    protected void validateLegConfiguration(List<ValidationIssue> issues, Process process) {
        Set<LegConfiguration> validLegs = process.getLegs();
        Legs legsXml = pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
        if (legsXml == null) {
            LOG.trace("legsXml is null, exiting");
            return;
        }
        List<Leg> allLegs = legsXml.getLeg();
        if (CollectionUtils.isEmpty(allLegs)) {
            createIssue(issues, process, "", "Empty legs are not allowed for the process [%s].");
            LOG.trace("legsXml is empty for process [{}], exiting", process);
            return;
        }
        allLegs.stream()
                .filter(leg -> validLegs.stream().noneMatch(validLeg -> validLeg.getName().equalsIgnoreCase(leg.getName())))
                .forEach(leg -> createIssue(issues, process, leg.getName(), "Leg [%s] of process [%s] not found in business process leg configurations"));
    }

    protected void createIssue(List<ValidationIssue> issues, Process process, String name, String message) {
        issues.add(pModeValidationHelper.createValidationIssue(message, name, process.getName()));
    }

}
