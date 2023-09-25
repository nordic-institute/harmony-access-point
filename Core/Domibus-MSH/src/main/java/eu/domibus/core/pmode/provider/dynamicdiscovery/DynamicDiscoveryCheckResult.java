package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.common.model.configuration.Party;

import java.security.cert.X509Certificate;

/**
 * Class responsible for holding the sending dynamic discovery query result
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class DynamicDiscoveryCheckResult {

    protected boolean performDynamicDiscovery;

    protected String finalRecipientCacheKey;
    protected EndpointInfo endpointInfo;
    protected PartyEndpointInfo partyEndpointInfo;

    protected Party pmodeReceiverParty;
    protected X509Certificate receiverCertificate;

    public boolean isPerformDynamicDiscovery() {
        return performDynamicDiscovery;
    }

    public void setPerformDynamicDiscovery(boolean performDynamicDiscovery) {
        this.performDynamicDiscovery = performDynamicDiscovery;
    }

    public EndpointInfo getEndpointInfo() {
        return endpointInfo;
    }

    public void setEndpointInfo(EndpointInfo endpointInfo) {
        this.endpointInfo = endpointInfo;
    }

    public PartyEndpointInfo getPartyEndpointInfo() {
        return partyEndpointInfo;
    }

    public void setPartyEndpointInfo(PartyEndpointInfo partyEndpointInfo) {
        this.partyEndpointInfo = partyEndpointInfo;
    }

    public Party getPmodeReceiverParty() {
        return pmodeReceiverParty;
    }

    public void setPmodeReceiverParty(Party pmodeReceiverParty) {
        this.pmodeReceiverParty = pmodeReceiverParty;
    }

    public X509Certificate getReceiverCertificate() {
        return receiverCertificate;
    }

    public void setReceiverCertificate(X509Certificate receiverCertificate) {
        this.receiverCertificate = receiverCertificate;
    }

    public String getFinalRecipientCacheKey() {
        return finalRecipientCacheKey;
    }

    public void setFinalRecipientCacheKey(String finalRecipientCacheKey) {
        this.finalRecipientCacheKey = finalRecipientCacheKey;
    }
}
