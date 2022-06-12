package eu.domibus.core.certificate;

import junit.framework.TestCase;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;

import static eu.domibus.core.certificate.CertificateHelper.JKS;
import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class CertificateHelperTest extends TestCase {

    @Tested
    CertificateHelper certificateHelper;

    @Test
    public void checkTruststoreTypeValidationHappy1() {
        certificateHelper.validateStoreType(JKS, "test.jks");
    }

    @Test
    public void checkTruststoreTypeValidationHappy2() {
        certificateHelper.validateStoreType(JKS, "test.JKS");
    }

    @Test
    public void checkTruststoreTypeValidationHappy3() {
        certificateHelper.validateStoreType("pkcs12", "test_filename.pfx");
    }

    @Test
    public void checkTruststoreTypeValidationHappy4() {
        certificateHelper.validateStoreType("pkcs12", "test_filename.p12");
    }

    @Test
    public void checkTruststoreTypeValidationNegative1() {
        try {
            certificateHelper.validateStoreType(JKS, "test_filename_wrong_extension.p12");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains(JKS));
        }
    }

    @Test
    public void checkTruststoreTypeValidationNegative2() {
        try {
            certificateHelper.validateStoreType(JKS, "test_filename_no_extension");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains(JKS));
        }
    }

    @Test
    public void checkTruststoreTypeValidationNegative3() {
        try {
            certificateHelper.validateStoreType("pkcs12", "test_filename_unknown_extension.txt");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains("pkcs12"));
        }
    }
}
