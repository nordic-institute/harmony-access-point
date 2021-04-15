package eu.domibus.api.model;

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
}
