package eu.domibus.core.message.nonrepudiation;

import java.nio.charset.StandardCharsets;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class RawEnvelopeDto {
    String rawMessage;
    final long id;

    public RawEnvelopeDto(long id, byte[] rawMessage) {
        this.id = id;
        if (rawMessage != null) {
            this.rawMessage = new String(rawMessage, StandardCharsets.UTF_8);
        }
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public long getId() {
        return id;
    }
}
