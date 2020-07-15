package eu.domibus.api.property;

import eu.domibus.api.property.validators.DomibusPropertyValidator;
import org.junit.Assert;
import org.junit.Test;

public class DomibusPropertyMetadataTypeTest {

    @Test
    public void testURIValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.URI.getValidator();

        Assert.assertTrue(validator.isValid("localhost"));
        Assert.assertTrue(validator.isValid("urn:oasis:names:tc:ebcore:partyid-type:unregistered"));
        Assert.assertTrue(validator.isValid("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder"));
        Assert.assertTrue(validator.isValid("/linux/path/"));
        Assert.assertTrue(validator.isValid("c:/windows/path with spaces/~!@#$%^&()_+-=[]{};',.`/this_is_a_valid_windows_path"));
        Assert.assertTrue(validator.isValid("file:///c:/windows/path/"));
        Assert.assertTrue(validator.isValid("https://some-other-url:1234/url.aspx?param1=val&param2=val2+aaa"));

        Assert.assertFalse(validator.isValid("this is invalid \r\b"));
        Assert.assertFalse(validator.isValid("this is invalid <"));
    }

    @Test
    public void testConcurrencyValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.CONCURRENCY.getValidator();

        Assert.assertTrue(validator.isValid("50"));
        Assert.assertTrue(validator.isValid("1-10"));

        Assert.assertFalse(validator.isValid("-1"));
        Assert.assertFalse(validator.isValid("?"));
    }

    @Test
    public void testClassValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.CLASS.getValidator();

        Assert.assertTrue(validator.isValid("eu.domibus.core.property.encryption.plugin.PasswordEncryptionExtServiceImplTest"));
        Assert.assertTrue(validator.isValid("com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl"));

        Assert.assertFalse(validator.isValid("?"));
    }

    @Test
    public void testJndiValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.JNDI.getValidator();

        Assert.assertTrue(validator.isValid("domibus.backend.jms.replyQueue"));
        Assert.assertTrue(validator.isValid("jms/domibus.backend.jms.replyQueue"));

        Assert.assertFalse(validator.isValid("aaa>aaa"));
    }

    @Test
    public void testEmailValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.EMAIL.getValidator();

        Assert.assertTrue(validator.isValid("abc_ABC@domibus-host.com"));
        Assert.assertTrue(validator.isValid("abc_ABC@domibus-host.com ; second.address@example.org "));
        Assert.assertFalse(validator.isValid("invalid@@email.com"));
    }

    @Test
    public void testCommaSeparatedListValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.COMMA_SEPARATED_LIST.getValidator();

        Assert.assertTrue(validator.isValid("domibus-blue, domibus-123"));
        Assert.assertFalse(validator.isValid("aaa;"));
    }

    @Test
    public void testHyphenedNameValidator() {
        DomibusPropertyValidator validator = DomibusPropertyMetadata.Type.HYPHENED_NAME.getValidator();

        Assert.assertTrue(validator.isValid("bdxr-transport-ebms3-as4-v1p0"));
        Assert.assertFalse(validator.isValid("abc'abc"));
    }
}
