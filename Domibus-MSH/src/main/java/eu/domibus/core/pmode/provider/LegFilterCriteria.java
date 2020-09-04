package eu.domibus.core.pmode.provider;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.Role;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author Arun Raj
 * @since 4.2
 */
public class LegFilterCriteria {
    private final String agreementName;
    private final String senderParty;
    private final String receiverParty;
    private final Role initiatorRole;
    private final Role responderRole;
    private final String service;
    private final String action;

    private Map<Process, String> processMismatchErrors;
    private Map<LegConfiguration, String> legMismatchErrors;

    public LegFilterCriteria(String agreementName, String senderParty, String receiverParty, Role initiatorRole, Role responderRole, String service, String action) {
        this.agreementName = agreementName;
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.initiatorRole = initiatorRole;
        this.responderRole = responderRole;
        this.service = service;
        this.action = action;

        this.processMismatchErrors = new HashMap<>();
        this.legMismatchErrors = new HashMap<>();
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getSenderParty() {
        return senderParty;
    }

    public String getReceiverParty() {
        return receiverParty;
    }

    public Role getInitiatorRole() {
        return initiatorRole;
    }

    public Role getResponderRole() {
        return responderRole;
    }

    public String getService() {
        return service;
    }

    public String getAction() {
        return action;
    }

    public Map<Process, String> getProcessMismatchErrors() {
        return processMismatchErrors;
    }

    public void appendProcessMismatchErrors(Process process, String newErrorDetail){
        if(process == null || StringUtils.isNotBlank(newErrorDetail)){
            return;
        }
        if (!processMismatchErrors.containsKey(process)) {
            processMismatchErrors.put(process, "For Process:[" + process.getName() + "]");
        }
        processMismatchErrors.put(process, processMismatchErrors.get(process).concat(", ").concat(newErrorDetail));
    }

    public List<Process> listProcessesWitMismatchErrors(){
        return new ArrayList<>(processMismatchErrors.keySet());
    }

    public String getProcessMismatchErrorDetails(){
        return String.join("\n", processMismatchErrors.values());
    }

    public Map<LegConfiguration, String> getLegMismatchErrors() {
        return legMismatchErrors;
    }

    public void appendLegMismatchErrors(LegConfiguration legConfiguration, String newErrorDetail){
        if(legConfiguration == null || StringUtils.isNotBlank(newErrorDetail)){
            return;
        }
        if (!legMismatchErrors.containsKey(legConfiguration)) {
            legMismatchErrors.put(legConfiguration, "For LegConfiguration:[" + legConfiguration.getName() + "]");
        }
        legMismatchErrors.put(legConfiguration, legMismatchErrors.get(legConfiguration).concat(", ").concat(newErrorDetail));
    }

    public Set<LegConfiguration> listLegConfigurationsWitMismatchErrors(){
        return legMismatchErrors.keySet();
    }

    public String getLegConfigurationMismatchErrorDetails(){
        return String.join("\n", legMismatchErrors.values());
    }
}
