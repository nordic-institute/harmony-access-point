package eu.domibus.core.crypto.spi.model;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class UserMessagePmodeData {

    private final String serviceName;

    private final String actionName;

    private final String partyName;

    private final String securityProfile;

    public UserMessagePmodeData(String serviceName, String actionName, String partyName, String securityProfile) {
        this.serviceName = serviceName;
        this.actionName = actionName;
        this.partyName = partyName;
        this.securityProfile = securityProfile;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getActionName() {
        return actionName;
    }

    public String getPartyName() {
        return partyName;
    }

    public String getSecurityProfile() {
        return securityProfile;
    }
}
