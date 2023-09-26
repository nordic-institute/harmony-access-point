package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.common.model.configuration.Party;

import java.security.cert.X509Certificate;

/**
 * DTO class responsible for holding the data following the check if the dynamic discovery in SMP should be triggered
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class DynamicDiscoveryCheckResult {

    /**
     * True if dynamic discovery in SMP should be triggered
     */
    protected boolean performDynamicDiscovery;

    /**
     * The key in the SMP lookup cache for the final recipient
     */
    protected String finalRecipientCacheKey;

    /**
     * The SMP dynamic discovery result if it is present in the cache
     */
    protected EndpointInfo endpointInfo;

    /**
     * The receiver party details extracted from the SMP lookup
     */
    protected PartyEndpointInfo partyEndpointInfo;

    /**
     * The receiver party if it is already present in the PMode->responderParties
     */
    protected Party pmodeReceiverParty;

    /**
     * The receiver party public certificate already present in the truststore(if available)
     */
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
