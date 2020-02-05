package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates
 */
@Component
@Order(1)
public class BusinessProcessValidator implements PModeValidator {

    @Autowired
    PModeValidationHelper pModeValidationHelper;

    @Override
    public List<PModeIssue> validate(Configuration pMode) {
        List<PModeIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getProcesses().forEach(process -> {

            //agreement
            if (process.getAgreement() == null) {
                String name = pModeValidationHelper.getAttributeValue(process, "agreementXml", String.class);
                //agreements can be null
                if (StringUtils.isNotEmpty(name)) {
                    createIssue(issues, process, name, "Agreement [%s] of process [%s] not found in business process agreements.");
                }
            }

            //mep
            if (process.getMep() == null) {
                String name = pModeValidationHelper.getAttributeValue(process, "mepXml", String.class);
                createIssue(issues, process, name, "Mep [%s] of process [%s] not found in business process meps.");
            }

            //binding
            if (process.getMepBinding() == null) {
                String name = pModeValidationHelper.getAttributeValue(process, "bindingXml", String.class);
                createIssue(issues, process, name, "Mep binding [%s] of process [%s] not found in business process bindings.");
            }

            //initiator Role
            if (process.getInitiatorRole() == null) {
                String name = pModeValidationHelper.getAttributeValue(process, "initiatorRoleXml", String.class);
                createIssue(issues, process, name, "Initiator role [%s] of process [%s] not found in business process roles.");
            }

            //responder Role
            if (process.getResponderRole() == null) {
                String name = pModeValidationHelper.getAttributeValue(process, "responderRoleXml", String.class);
                createIssue(issues, process, name, "Responder role [%s] of process [%s] not found in business process roles.");
            }

            //initiator Parties
            Set<Party> validInitiatorParties = process.getInitiatorParties();
            InitiatorParties initiatorPartiesXml = process.getInitiatorPartiesXml(); // pModeValidationHelper.getAttributeValue(process, "initiatorPartiesXml", InitiatorParties.class);
            if (initiatorPartiesXml != null) {
                List<InitiatorParty> allInitiatorParties = initiatorPartiesXml.getInitiatorParty();
                if (!CollectionUtils.isEmpty(allInitiatorParties) && allInitiatorParties.size() != validInitiatorParties.size()) {
                    allInitiatorParties.stream()
                            .filter(el -> validInitiatorParties.stream().noneMatch(el2 -> el2.getName().equals(el.getName())))
                            .forEach(party -> {
                                createIssue(issues, process, party.getName(), "Initiator party [%s] of process [%s] not found in business process parties");
                            });
                }
            }

            //responder Parties
            Set<Party> validResponderParties = process.getResponderParties();
            ResponderParties responderPartiesXml = process.getResponderPartiesXml(); // pModeValidationHelper.getAttributeValue(process, "responderPartiesXml", ResponderParties.class);
            if (responderPartiesXml != null) {
                List<ResponderParty> allResponderParties = responderPartiesXml.getResponderParty();
                if (!CollectionUtils.isEmpty(allResponderParties) && allResponderParties.size() != validResponderParties.size()) {
                    allResponderParties.stream()
                            .filter(el -> validResponderParties.stream().noneMatch(el2 -> el2.getName().equals(el.getName())))
                            .forEach(party -> {
                                createIssue(issues, process, party.getName(), "Responder party [%s] of process [%s] not found in business process parties");
                            });
                }
            }

            //leg configuration
            Set<LegConfiguration> validLegs = process.getLegs();
            Legs legsXml = pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
            if (legsXml != null) {
                List<Leg> allLegs = legsXml.getLeg();
                if (!CollectionUtils.isEmpty(allLegs) && allLegs.size() != validResponderParties.size()) {
                    allLegs.stream()
                            .filter(el -> validLegs.stream().noneMatch(el2 -> el2.getName().equals(el.getName())))
                            .forEach(party -> {
                                createIssue(issues, process, party.getName(), "Leg [%s] of process [%s] not found in business process leg configurations");
                            });
                }
            }

        });

        return issues;
    }

    private void createIssue(List<PModeIssue> issues, Process process, String name, String message) {
        issues.add(pModeValidationHelper.createIssue(message, name, process.getName()));
    }

}
