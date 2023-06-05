package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.backend.rules.WSPluginDispatchRule;

import java.util.List;

/**
 * @author François Gautier
 * @version 5.0
 */
public class RulesPerRecipient {
    String finalRecipient;
    List<WSPluginDispatchRule> rules;

    public RulesPerRecipient(String finalRecipient, List<WSPluginDispatchRule> rules) {
        this.finalRecipient = finalRecipient;
        this.rules = rules;
    }

    public String getFinalRecipient() {
        return finalRecipient;
    }

    public List<WSPluginDispatchRule> getRules() {
        return rules;
    }

}
