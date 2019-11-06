package eu.domibus.core.party;

import eu.domibus.api.validators.CustomWhiteListed;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public class CertificateContentRo {

    @CustomWhiteListed(permitted = "/+-=\n ") // base64 characters
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
