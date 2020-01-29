package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Party class for external API
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class PartyDTO {

    protected Integer entityId;

    protected Set<PartyIdentifierDTO> identifiers;

    protected List<ProcessDTO> processesWithPartyAsInitiator = new ArrayList<>();

    protected List<ProcessDTO> processesWithPartyAsResponder = new ArrayList<>();

    protected String name;

    protected String userName;

    protected String endpoint;

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Set<PartyIdentifierDTO> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<PartyIdentifierDTO> identifiers) {
        this.identifiers = identifiers;
    }

    public List<ProcessDTO> getProcessesWithPartyAsInitiator() {
        return processesWithPartyAsInitiator;
    }

    public void setProcessesWithPartyAsInitiator(List<ProcessDTO> processesWithPartyAsInitiator) {
        this.processesWithPartyAsInitiator = processesWithPartyAsInitiator;
    }

    public List<ProcessDTO> getProcessesWithPartyAsResponder() {
        return processesWithPartyAsResponder;
    }

    public void setProcessesWithPartyAsResponder(List<ProcessDTO> processesWithPartyAsResponder) {
        this.processesWithPartyAsResponder = processesWithPartyAsResponder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("entityId", entityId)
                .append("identifiers", identifiers)
                .append("processesWithPartyAsInitiator", processesWithPartyAsInitiator)
                .append("processesWithPartyAsResponder", processesWithPartyAsResponder)
                .append("name", name)
                .append("userName", userName)
                .append("endpoint", endpoint)
                .toString();
    }
}
