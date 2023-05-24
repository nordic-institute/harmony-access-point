package eu.domibus.api.model;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class RawEnvelopeDto {

    protected final byte[] rawMessage;
    protected final long id;
    protected final boolean compressed;

    /* The entity id of the UserMessage or SignalMessage */
    protected Long parentEntityId = null;

    public RawEnvelopeDto(long id, byte[] rawMessage, boolean compressed, Long parentEntityId) {
        this.id = id;
        this.rawMessage = rawMessage;
        this.compressed = compressed;
        this.parentEntityId = parentEntityId;
    }

    public RawEnvelopeDto(long id, byte[] rawMessage, boolean compressed) {
        this.id = id;
        this.rawMessage = rawMessage;
        this.compressed = compressed;
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public byte[] getRawMessage() {
        return rawMessage;
    }

    public long getId() {
        return id;
    }

    public String getRawXmlMessage() {
        final String rawXml = new String(getUncompressedRawData(), StandardCharsets.UTF_8);
        return rawXml;
    }

    public InputStream getRawXmlMessageAsStream() {
        return new ByteArrayInputStream(getUncompressedRawData());
    }

    private byte[] getUncompressedRawData() {
        if (!this.compressed) {
            return getRawMessage();
        }
        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(getRawMessage()))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Failed to unzip raw envelope data with id " + id, e);
        }
    }

}
