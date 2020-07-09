package eu.domibus.core.message;

public interface UserMessagePriorityService {

    Integer getPriority(String service, String action);
}
