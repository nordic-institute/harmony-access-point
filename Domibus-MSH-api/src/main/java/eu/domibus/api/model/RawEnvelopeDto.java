package eu.domibus.api.model;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class RawEnvelopeDto {
    final String rawMessage;
    final long id;

    public RawEnvelopeDto(long id, String rawMessage) {
        this.id = id;
        this.rawMessage = rawMessage;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public long getId() {
        return id;
    }
}
