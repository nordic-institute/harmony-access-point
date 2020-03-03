package eu.domibus.core.party;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.api.validators.SkipWhiteListed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyResponseRo {

    protected Integer entityId;

    protected Set<IdentifierRo> identifiers; //NOSONAR

    protected String name;

    protected String userName;

    /**
     * Custom annotation to add some additional characters to be permitted by black-list/white-list validation
     * The endpoint property can contain the specified characters so we must permit this
     */
    @CustomWhiteListed(permitted = ":/=?&-+%")
    protected String endpoint;

    /**
     * Custom annotation to skip the black-list validation altogether because this field is calculated on the fly and does not get persisted
     */
    @SkipWhiteListed
    private String joinedIdentifiers;

    /**
     * Custom annotation to skip the black-list validation altogether because this field is calculated on the fly and does not get persisted
     */
    @SkipWhiteListed
    private String joinedProcesses;

    private List<ProcessRo> processesWithPartyAsInitiator = new ArrayList<>();

    private List<ProcessRo> processesWithPartyAsResponder = new ArrayList<>();

    /**
     * Custom annotation to add some additional characters to be permitted by black-list/white-list validation
     * The certificate content property can contain the specified characters so we must permit this
     */
    @CustomWhiteListed(permitted = "/+-=\n ") // base64 characters
    protected String certificateContent;

    public Set<IdentifierRo> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<IdentifierRo> identifiers) {
        this.identifiers = identifiers;
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

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getJoinedIdentifiers() {
        return joinedIdentifiers;
    }

    public void setJoinedIdentifiers(String joinedIdentifiers) {
        this.joinedIdentifiers = joinedIdentifiers;
    }

    public String getJoinedProcesses() {
        return joinedProcesses;
    }

    public void setJoinedProcesses(String joinedProcesses) {
        this.joinedProcesses = joinedProcesses;
    }

    public List<ProcessRo> getProcessesWithPartyAsInitiator() {
        return processesWithPartyAsInitiator;
    }

    public void setProcessesWithPartyAsInitiator(List<ProcessRo> processesWithPartyAsInitiator) {
        this.processesWithPartyAsInitiator = processesWithPartyAsInitiator;
    }

    public List<ProcessRo> getProcessesWithPartyAsResponder() {
        return processesWithPartyAsResponder;
    }

    public void setProcessesWithPartyAsResponder(List<ProcessRo> processesWithPartyAsResponder) {
        this.processesWithPartyAsResponder = processesWithPartyAsResponder;
    }

    public String getCertificateContent() {
        return certificateContent;
    }

    public void setCertificateContent(String certificateContent) {
        this.certificateContent = certificateContent;
    }
}
