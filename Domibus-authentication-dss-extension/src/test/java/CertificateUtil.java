import eu.europa.esig.dss.utils.Utils;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateUtil {

    public static void main(String[] args) {
        final String base64X509Certificate = CertificateUtil.getBase64X509Certificate("C:\\install\\T_TeleSec_GlobalRoot_Class_2.cer");
        System.out.println(base64X509Certificate);
    }

    public static String getBase64X509Certificate(String x509Path) {
        try {
            //ski 0xBF59 2036 0079 A0A0 226B 8CD5 F261 D2B8 2CCB 824A
            final CertificateFactory instance = CertificateFactory
                    .getInstance("X.509");
            X509Certificate cer = (X509Certificate) instance.generateCertificate(new FileInputStream(x509Path));
            final byte[] extensionValue = cer.getExtensionValue(Extension.subjectKeyIdentifier.getId());
            if (Utils.isArrayNotEmpty(extensionValue)) {
                ASN1Primitive extension = X509ExtensionUtil.fromExtensionValue(extensionValue);
                SubjectKeyIdentifier skiBC = SubjectKeyIdentifier.getInstance(extension);
                System.out.println(Base64.getEncoder().encodeToString(skiBC.getEncoded()));
            }
            return Base64.getEncoder().encodeToString(cer.getEncoded());
        } catch (CertificateException |IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
