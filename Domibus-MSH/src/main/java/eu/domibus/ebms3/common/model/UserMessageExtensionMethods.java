package eu.domibus.ebms3.common.model;

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * Default interface used to add safe extension methods to the UserMessage class; it is like a mixin
 */
public interface UserMessageExtensionMethods {
    UserMessage getUserMessage();

    default String getFromFirstPartyId() {
        UserMessage um = getUserMessage();
        if (um != null && um.getPartyInfo() != null && um.getPartyInfo().getFrom() != null) {
            return um.getPartyInfo().getFrom().getFirstPartyId();
        }
        return null;
    }

    default String getToFirstPartyId() {
        UserMessage um = getUserMessage();
        if (um != null && um.getPartyInfo() != null && um.getPartyInfo().getTo() != null) {
            return um.getPartyInfo().getTo().getFirstPartyId();
        }
        return null;
    }
}
