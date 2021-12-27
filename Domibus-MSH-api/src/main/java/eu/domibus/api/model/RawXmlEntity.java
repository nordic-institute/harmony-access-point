package eu.domibus.api.model;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@MappedSuperclass
public class RawXmlEntity extends AbstractNoGeneratedPkEntity {

    @Lob
    @Column(name = "RAW_XML")
    protected byte[] rawXML;

    @Column(name = "COMPRESSED")
    protected Boolean compressed;

    public byte[] getRawXML() {
        if (rawXML == null) {
            return null;
        }

        if (!this.getCompressed()) {
            return this.rawXML;
        }

        try (GZIPInputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(rawXML))) {
            return IOUtils.toByteArray(unzipStream);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Decompression failed", e);
        }
    }

    public void setRawXML(String rawXML) {
        byte[] bytes = rawXML.getBytes(StandardCharsets.UTF_8);
        this.setRawXML(bytes);
    }

    protected void setRawXML(byte[] rawXML) {
        if (rawXML == null) {
            this.rawXML = null;
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(rawXML.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
            zipStream.write(rawXML);
        } catch (IOException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_008, "Compression failed", e);
        }

        this.rawXML = byteStream.toByteArray();
        this.compressed = true;
    }

    public Boolean getCompressed() {
        return BooleanUtils.isTrue(compressed);
    }

    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }
}
