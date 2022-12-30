package eu.domibus.api.security;


import java.security.cert.X509Certificate;

public interface X509CertificateService {

    void validateClientX509Certificates(final X509Certificate... certificate) throws AuthenticationException;
}
