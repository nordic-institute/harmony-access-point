package eu.domibus.api.property.encryption;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionResult {

    protected String propertyName;

    protected String propertyValue;

    protected String base64EncryptedValue;

    protected String formattedBase64EncryptedValue;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getBase64EncryptedValue() {
        return base64EncryptedValue;
    }

    public void setBase64EncryptedValue(String base64EncryptedValue) {
        this.base64EncryptedValue = base64EncryptedValue;
    }

    public String getFormattedBase64EncryptedValue() {
        return formattedBase64EncryptedValue;
    }

    public void setFormattedBase64EncryptedValue(String formattedBase64EncryptedValue) {
        this.formattedBase64EncryptedValue = formattedBase64EncryptedValue;
    }
}
