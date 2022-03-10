package eu.domibus.api.multitenancy;

public interface DomainMessageListenerContainer {
    String getName();
    Domain getDomain();
}
