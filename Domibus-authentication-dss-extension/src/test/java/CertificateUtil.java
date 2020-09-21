import eu.europa.esig.dss.utils.Utils;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateUtil {

    public static void main(String[] args) throws CertificateException {
       // final String base64X509Certificate = CertificateUtil.getBase64X509Certificate("C:\\install\\T_TeleSec_GlobalRoot_Class_2.cer");
        //System.out.println(base64X509Certificate);
        CertificateUtil.getCertificateFromBase64();
    }

    public static String getBase64X509Certificate(String x509Path) {
        try {
            //ski 0xBF59 2036 0079 A0A0 226B 8CD5 F261 D2B8 2CCB 824A
            final CertificateFactory instance = CertificateFactory
                    .getInstance("X.509");
            X509Certificate cer = (X509Certificate) instance.generateCertificate(new FileInputStream(x509Path));
            System.out.println(cer.toString());
            final byte[] extensionValue = cer.getExtensionValue(Extension.subjectKeyIdentifier.getId());
            if (Utils.isArrayNotEmpty(extensionValue)) {
                ASN1Primitive extension = X509ExtensionUtil.fromExtensionValue(extensionValue);
                SubjectKeyIdentifier skiBC = SubjectKeyIdentifier.getInstance(extension);
                System.out.println("Subject key identifier "+skiBC.getKeyIdentifier().toString());
                System.out.println(Base64.getEncoder().encodeToString(skiBC.getEncoded()));
            }
            return Base64.getEncoder().encodeToString(cer.getEncoded());
        } catch (CertificateException |IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void getCertificateFromBase64() throws CertificateException {
        String certString="MIIEwDCCA6igAwIBAgIBJTANBgkqhkiG9w0BAQsFADBQMQswCQYDVQQGEwJERTEiMCAGA1UECgwZQnVuZGVzYWdlbnR1ciBmdWVyIEFyYmVpdDEdMBsGA1UEAwwUQkEtUUMtV3VyemVsLUNBLTE6UE4wHhcNMTIxMTE1MDg0ODI3WhcNMTcxMTE0MDg0ODI3WjBLMQswCQYDVQQGEwJERTEiMCAGA1UECgwZQnVuZGVzYWdlbnR1ciBmdWVyIEFyYmVpdDEYMBYGA1UEAwwPQkEtUUMtVFNQLTEwOlBOMIIBJDANBgkqhkiG9w0BAQEFAAOCAREAMIIBDAKCAQEA27/47vQz0iluHiQMQJRDTi4dxA8TFlHyPr/tkZ4JXTjVQ6W5fmmruczdhtsWPuapq0LxuTvSZ3VLuet/VT4bVjieaiIvVC4YbCuOGle67rVj7G8I8+rSB0/7Tti+KRYC3qEDE8W7K/li+5fm/3AZKZ/J9jdsjbJ0vBxF1IFRkK43HBOD2EsUb+YJg2JMOcGMu4N9zaAI4LFLXjpBUM9bUn3qtSpRFSm6HFP9UfkoqR+CK2kWuA4MZuFAo3D+662BCfkMq7jpCJcSewFY1kYB8Bazq4Oc4k23JRzjcFMjZbIjewuKqzQtx9+FwZmeac0BU/B45iesrluoktr6clIq3QIFAMPy+p2jggGmMIIBojAfBgNVHSMEGDAWgBRYXYNaZ8zSYdr18GEjAc+cGB7GUjAdBgNVHQ4EFgQUl7OB7h0UXDHF43Yj4wAzq52slXswDgYDVR0PAQH/BAQDAgZAMBoGA1UdIAQTMBEwDwYNKwYBBAGBqS8BAQUCADAPBgNVHRMBAf8EBTADAQEAMBYGA1UdJQEB/wQMMAoGCCsGAQUFBwMIMIGDBgNVHR8EfDB6MHigdqB0hnJsZGFwOi8vbGRhcC5wa2kuYXJiZWl0c2FnZW50dXIuZGUvb3U9QkEtUUMtV3VyemVsLUNBLTE6UE4sbz1CdW5kZXNhZ2VudHVyIGZ1ZXIgQXJiZWl0LGM9REU/YXV0aG9yaXR5UmV2b2NhdGlvbkxpc3QwgYQGCCsGAQUFBwEBBHgwdjB0BggrBgEFBQcwAoZobGRhcDovL2xkYXAucGtpLmFyYmVpdHNhZ2VudHVyLmRlL291PUJBLVFDLVd1cnplbC1DQS0xOlBOLG89QnVuZGVzYWdlbnR1ciBmdWVyIEFyYmVpdCxjPURFP2NBQ2VydGlmaWNhdGUwDQYJKoZIhvcNAQELBQADggEBAGc1IOZjL/XJadeq+59KwYUO9n6MiVKB/uYGAJJJtYvcuuqV30Ow+97E7ipoAA1F7rrjV3amYSxOEHlE1U1bR5//Ujy223rhkQIuyRTQsnchsmbBhwiSyPbsd9miIcumSR93vcxB95WzdKY87iTfI5hixNM3ChW+uuaoJm7dbG+an9cRUSvO6cYvqZm/fXZ7zW4vkmy9Kzk5JHn2DFpkoHJ+mz/VHfj83fWJ+18moK43va/WOvUkGO9rCjtHZbDUQ4zzWOR9KAJxuWW9ud6d7bB5X/fP+Qw9j7GPJRq3jSpOs6Li5eGQ5gEfMOt73QgxAVk30DVPYFS0S7dTe/aJev0=";
        byte encodedCert[] = Base64.getDecoder().decode(certString);
        ByteArrayInputStream inputStream  =  new ByteArrayInputStream(encodedCert);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(inputStream);
        System.out.println("Cert : "+cert);
    }
}
