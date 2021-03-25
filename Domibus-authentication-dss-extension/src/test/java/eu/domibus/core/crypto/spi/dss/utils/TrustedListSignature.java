package eu.domibus.core.crypto.spi.dss.utils;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.JKSSignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.TrustedListSignatureParametersBuilder;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class TrustedListSignature {

    public TrustedListSignature() throws IOException {

        JKSSignatureToken signingToken = new JKSSignatureToken("C:\\tmp\\gateway_keystore.jks", new KeyStore.PasswordProtection("test123".toCharArray()));
        DSSDocument trustedList = new FileDocument("Domibus-authentication-dss-extension/src/test/resources/CUST.xml");

        DSSPrivateKeyEntry privateKeyEntry = signingToken.getKeys().get(0);
        CertificateToken signingCertificate = privateKeyEntry.getCertificate();
// optionally the certificate chain can be provided
        List<CertificateToken> certificateChain = Arrays.asList(privateKeyEntry.getCertificateChain());

// This class creates the appropriated XAdESSignatureParameters object to sign a trusted list.
// It handles the configuration complexity and creates a ready-to-be-used XAdESSignatureParameters with the packaging, the references, the canononicalization,...
        TrustedListSignatureParametersBuilder builder = new TrustedListSignatureParametersBuilder(signingCertificate, certificateChain, trustedList);
        XAdESSignatureParameters parameters = builder.build();

        XAdESService service = new XAdESService(new CommonCertificateVerifier());

        ToBeSigned dataToSign = service.getDataToSign(trustedList, parameters);
        SignatureValue signatureValue = signingToken.sign(dataToSign, parameters.getDigestAlgorithm(), privateKeyEntry);
        DSSDocument signedTrustedList = service.signDocument(trustedList, parameters, signatureValue);
        signedTrustedList.writeTo(new FileOutputStream("C:\\tmp\\CUST.xml"));
    }

    public static void main(String[] args) throws IOException {
        new TrustedListSignature();
    }
}
