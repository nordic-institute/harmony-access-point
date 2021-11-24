package eu.domibus.api.model;

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
        final String rawXml = new String(getUncompressedRawData(), StandardCharsets.UTF_8);
        return rawXml;
    }

    public InputStream getRawXmlMessageAsStream() {
        return new ByteArrayInputStream(getUncompressedRawData());
    }

    private byte[] getUncompressedRawData() {
        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(getRawMessage()))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            // TODO
            return getRawMessage(); // not compressed? return the raw data
        }
    }

}
