package eu.domibus.core.crypto.spi.dss;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXReason;
import java.util.List;

public class ValidationReportError {


    private static final Logger LOG = LoggerFactory.getLogger(DomibusDssCryptoSpi.class);

    public static final String BBB_XCV_CCCBB = "BBB_XCV_CCCBB";

    public void wsSecurityException(final List<String> constraintNames) throws WSSecurityException {
        if (constraintNames.isEmpty()) {
            return;
        }

        LOG.error("Dss triggered and error while validating the certificate chain:[{}]", reports.getXmlSimpleReport());
        String constraintName = constraintNames.get(0);
        switch (constraintName) {
            case ValidationReport.INVALID_CONSTRAINT_NAME:

            case BBB_XCV_CCCBB:
                throw new WSSecurityException(
                        WSSecurityException.ErrorCode.FAILURE, new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1, PKIXReason.NO_TRUST_ANCHOR), "certpath"
                );


        }


    }
}
