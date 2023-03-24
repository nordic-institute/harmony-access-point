package eu.domibus.api.message.validation;

import java.io.InputStream;

public interface UserMessageValidatorServiceDelegate {

    void validate(eu.domibus.api.usermessage.domain.UserMessage userMessage);

    void validatePayload(InputStream payload, String mimeType);
}
