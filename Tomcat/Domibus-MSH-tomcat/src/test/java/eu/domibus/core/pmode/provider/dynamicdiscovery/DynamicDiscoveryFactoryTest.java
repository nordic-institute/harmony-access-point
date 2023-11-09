package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.europa.ec.dynamicdiscovery.model.SMPEndpoint;
import eu.europa.ec.dynamicdiscovery.model.SMPServiceMetadata;
import eu.europa.ec.dynamicdiscovery.model.SMPTransportProfile;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPParticipantIdentifier;
import eu.europa.ec.dynamicdiscovery.model.identifiers.SMPProcessIdentifier;

import java.security.cert.X509Certificate;

public class DynamicDiscoveryFactoryTest {

    public static SMPServiceMetadata buildServiceMetadata(
            SMPProcessIdentifier processIdentifier,
            String transportProfile,
            String url,
            X509Certificate certificate,
            String finalRecipient) {
           final SMPEndpoint smpEndpoint = new SMPEndpoint.Builder()
                .addProcessIdentifier(processIdentifier)
                .transportProfile(new SMPTransportProfile(transportProfile))
                .address(url)
                .addCertificate(SMPEndpoint.DEFAULT_CERTIFICATE, certificate)
                .build();

        return new SMPServiceMetadata.Builder()
                .addEndpoint(smpEndpoint)
                .participantIdentifier(new SMPParticipantIdentifier(finalRecipient, "iso6523-actorid-upis"))
                .build();
    }

    public static String getAccessPointUrl(String partyName) {
        return "http://localhost:9090/" + partyName + "/msh";
    }
}
