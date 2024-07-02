
package eu.domibus.api.message.compression;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DecompressionDataSource implements DataSource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DecompressionDataSource.class);

    private final DataSource source;
    private final String mime;

    public DecompressionDataSource(final DataSource source, final String mime) {
        this.source = source;
        this.mime = mime;
    }


    @Override
    public InputStream getInputStream() throws IOException {
        LOG.debug("Decompress data source with mimeType: [{}]", mime);
        return new GZIPInputStream(source.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return mime;
    }

    @Override
    public String getName() {
        return "decompressionDataSource - " + source.getName();
    }
}
