package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Party class for external API
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class PartyDTO {

    private String name;

    private String userName;

    private String endpoint;

    private List<PartyIdentifierDTO> identifiers;

    private List<ProcessDTO> processesWithPartyAsInitiator = new ArrayList<>();

    private List<ProcessDTO> processesWithPartyAsResponder = new ArrayList<>();

    private String certificateContent;

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

    public List<PartyIdentifierDTO> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<PartyIdentifierDTO> identifiers) {
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

    public String getCertificateContent() {
        return certificateContent;
    }

    public void setCertificateContent(String certificateContent) {
        this.certificateContent = certificateContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("userName", userName)
                .append("endpoint", endpoint)
                .append("identifiers", identifiers)
                .append("processesWithPartyAsInitiator", processesWithPartyAsInitiator)
                .append("processesWithPartyAsResponder", processesWithPartyAsResponder)
                .append("certificateContent", certificateContent)
                .toString();
    }
}
