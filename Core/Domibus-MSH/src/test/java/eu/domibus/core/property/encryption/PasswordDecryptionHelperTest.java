package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PasswordDecryptionHelperTest {

    @Tested
    PasswordDecryptionHelper passwordDecryptionHelper;

    @Test
    public void isValueEncryptedWithNonEncryptedValue() {
        Assert.assertFalse(passwordDecryptionHelper.isValueEncrypted("nonEncrypted"));
    }

    @Test
    public void isValueEncryptedWithEncryptedValue() {
        Assert.assertTrue(passwordDecryptionHelper.isValueEncrypted("ENC(nonEncrypted)"));
    }

    @Test
    public void isValueEncrypted_blank() {
        assertFalse(passwordDecryptionHelper.isValueEncrypted(""));
    }

}