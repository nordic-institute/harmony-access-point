package eu.domibus.api.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class RawEnvelopeDto {
    final byte[] rawMessage;
    final long id;

    public RawEnvelopeDto(long id, byte[] rawMessage) {
        this.id = id;
        this.rawMessage = rawMessage;
    }

    public byte[] getRawMessage() {
        return rawMessage;
    }

    public long getId() {
        return id;
    }

    public String getRawXmlMessage() {
        final String rawXml = new String(rawMessage, StandardCharsets.UTF_8);
        return rawXml;
    }

    public InputStream getRawXmlMessageAsStream() {
        return new ByteArrayInputStream(rawMessage);
    }
}
