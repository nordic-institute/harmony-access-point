package eu.domibus.core.pmode.provider.dynamicdiscovery;


import network.oxalis.vefa.peppol.common.model.ServiceMetadata;

import java.security.cert.X509Certificate;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class FinalRecipientConfiguration {
        protected X509Certificate certificate;
        protected ServiceMetadata serviceMetadata;

        protected String partyName;

        public FinalRecipientConfiguration(X509Certificate certificate, ServiceMetadata serviceMetadata, String partyName) {
            this.certificate = certificate;
            this.serviceMetadata = serviceMetadata;
            this.partyName = partyName;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(X509Certificate certificate) {
            this.certificate = certificate;
        }

        public ServiceMetadata getServiceMetadata() {
            return serviceMetadata;
        }

        public void setServiceMetadata(ServiceMetadata serviceMetadata) {
            this.serviceMetadata = serviceMetadata;
        }

        public String getPartyName() {
            return partyName;
        }

        public void setPartyName(String partyName) {
            this.partyName = partyName;
        }
    }
