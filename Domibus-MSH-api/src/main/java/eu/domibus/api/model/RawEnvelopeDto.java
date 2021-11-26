package eu.domibus.api.model;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawEnvelopeDto.class);

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
            LOG.warn("Failed to unzip raw envelope data with id [{}]", id, e);
            // TODO EDELIVERY-8704 there will be no uncompressed envelopes in the database after migration
            // for now, we just try to decompress them and, if failing, return the raw data
            return getRawMessage();
        }
    }

}
