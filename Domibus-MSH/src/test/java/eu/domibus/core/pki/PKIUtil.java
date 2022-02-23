package eu.domibus.core.pki;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Cosmin Baciu on 08-Jul-16.
 */
public class PKIUtil {

    public X509CRL createCRL(List<BigInteger> revokedSerialNumbers) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        KeyPair caKeyPair = generateKeyPair();

        X509V2CRLGenerator crlGen = new X509V2CRLGenerator();
        Date now = new Date();
        crlGen.setIssuerDN(new X500Principal("CN=GlobalSign Root CA"));
        crlGen.setThisUpdate(now);
        crlGen.setNextUpdate(new Date(now.getTime() + 60 * 1000));
        crlGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        if (revokedSerialNumbers != null) {
            for (BigInteger revokedSerialNumber : revokedSerialNumbers) {
                crlGen.addCRLEntry(revokedSerialNumber, now, CRLReason.privilegeWithdrawn);
            }
        }

        crlGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caKeyPair.getPublic()));
        crlGen.addExtension(X509Extensions.CRLNumber, false, new CRLNumber(BigInteger.valueOf(1)));

        return crlGen.generateX509CRL(caKeyPair.getPrivate(), "BC");
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public X509Certificate createCertificate(BigInteger serial, Date startDate, Date expiryDate, List<String> crlUrls) throws SignatureException, NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException, CertificateEncodingException {
        KeyPair key = generateKeyPair();

        X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setSubjectDN(new X509Name("C=BE, O=GlobalSign nv-sa, OU=Root CA"));
        X500Principal subjectName = new X500Principal("CN=GlobalSign Root CA");
        generator.setIssuerDN(subjectName);
        generator.setSerialNumber(serial);
        generator.setNotBefore(startDate);
        generator.setNotAfter(expiryDate);
        generator.setPublicKey(key.getPublic());
        generator.setSignatureAlgorithm("SHA256WithRSAEncryption");

        if (crlUrls != null) {
            DistributionPoint[] distPoints = createDistributionPoints(crlUrls);
            generator.addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(distPoints));
        }

        X509Certificate x509Certificate = generator.generate(key.getPrivate(), "BC");
        return x509Certificate;
    }

    public X509Certificate createCertificate(BigInteger serial, List<String> crlUrls) throws SignatureException, NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException, CertificateEncodingException {
        return createCertificate(serial, new Date(), new Date(), crlUrls);
    }

    public DistributionPoint[] createDistributionPoints(List<String> crlUrls) {
        List<DistributionPoint> result = new ArrayList<>();
        for (String crlUrl : crlUrls) {
            DistributionPointName distPointOne = new DistributionPointName(
                    new GeneralNames(
                            new GeneralName(GeneralName.uniformResourceIdentifier, crlUrl)
                    )
            );
            result.add(new DistributionPoint(distPointOne, null, null));
        }


        return result.toArray(new DistributionPoint[0]);
    }


    public X509Certificate createCertificate(BigInteger serial, List<String> crlUrls, List<String> policies) throws
            NoSuchAlgorithmException, CertificateException, OperatorCreationException, IOException {
        return generateCertificate("CN=test,OU=Domibus,O=eDelivery,C=EU", serial,
                DateUtils.addDays(Calendar.getInstance().getTime(), -1),
                DateUtils.addDays(Calendar.getInstance().getTime(), 1), null, null, crlUrls, null, null,
                "SHA256WithRSAEncryption", policies);
    }

    /**
     * Generic method for generating test certificatese
     *
     * @param subjectDn                  - subject certificate
     * @param serial                     - serial number
     * @param notBefore                  - start valid period for certificate
     * @param notAfter-                  end valid period for certificate
     * @param issuerCertificate          - issuer certificate
     * @param issuerPrivateKeyForSigning - certificate signing key
     * @param crlUris                    - list of CRLs
     * @param ocspUri                    - OCSP URO
     * @param keyUsage                   - key usage
     * @param signatureAlgorithm         - signature algorithms
     * @param certificatePolicies        - certificate policy
     * @return
     * @throws IllegalStateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    public X509Certificate generateCertificate(String subjectDn, BigInteger serial,
                                               Date notBefore, Date notAfter, X509Certificate issuerCertificate, PrivateKey issuerPrivateKeyForSigning,
                                               List<String> crlUris, String ocspUri, KeyUsage keyUsage,
                                               String signatureAlgorithm,
                                               List<String> certificatePolicies
    ) throws IllegalStateException, IOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException {


        KeyPair certificateKey = generateKeyPair();

        X500Name issuerName;
        if (ObjectUtils.isNotEmpty(issuerCertificate)) {
            issuerName = new X500Name(issuerCertificate.getSubjectX500Principal().toString());
        } else {
            issuerName = new X500Name(subjectDn);
        }
        X500Name subjectName = new X500Name(subjectDn);
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(certificateKey.getPublic().getEncoded());
        X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(issuerName, serial,
                notBefore, notAfter, subjectName, publicKeyInfo);

        // add CRL DistributionPoints
        if (crlUris != null && !crlUris.isEmpty()) {
            List<DistributionPoint> distributionPoints =
                    crlUris.stream().map(crlUri -> {
                        GeneralName generalName = new GeneralName(GeneralName.uniformResourceIdentifier,
                                new DERIA5String(crlUri));
                        GeneralNames generalNames = new GeneralNames(generalName);
                        DistributionPointName distPointName = new DistributionPointName(generalNames);
                        return new DistributionPoint(distPointName, null, null);
                    }).collect(Collectors.toList());
            DistributionPoint[] crlDistPoints = distributionPoints.toArray(new DistributionPoint[]{});
            CRLDistPoint crlDistPoint = new CRLDistPoint(crlDistPoints);
            x509v3CertificateBuilder.addExtension(Extension.cRLDistributionPoints, false, crlDistPoint);

        }

        // add OCSP URI
        if (StringUtils.isNotBlank(ocspUri)) {
            GeneralName ocspName = new GeneralName(GeneralName.uniformResourceIdentifier, ocspUri);
            AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess(X509ObjectIdentifiers.ocspAccessMethod, ocspName);
            x509v3CertificateBuilder.addExtension(Extension.authorityInfoAccess, false, authorityInformationAccess);
        }

        if (ObjectUtils.isNotEmpty(keyUsage)) {
            x509v3CertificateBuilder.addExtension(Extension.keyUsage, true, keyUsage);
        }
        // add certificate policies
        if (certificatePolicies != null && !certificatePolicies.isEmpty()) {
            List<PolicyInformation> policyInformationList = certificatePolicies.stream().map(certificatePolicy -> {
                ASN1ObjectIdentifier policyObjectIdentifier = new ASN1ObjectIdentifier(certificatePolicy);
                return new PolicyInformation(policyObjectIdentifier);
            }).collect(Collectors.toList());

            x509v3CertificateBuilder.addExtension(Extension.certificatePolicies, false,
                    new DERSequence(policyInformationList.toArray(new PolicyInformation[]{})));

        }

        // generate certificate
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm);
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(
                issuerPrivateKeyForSigning == null ? certificateKey.getPrivate().getEncoded() : issuerPrivateKeyForSigning.getEncoded()
        );


        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
                .build(asymmetricKeyParameter);
        X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(contentSigner);

        byte[] encodedCertificate = x509CertificateHolder.getEncoded();

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory
                .generateCertificate(new ByteArrayInputStream(encodedCertificate));
        return certificate;
    }

}
