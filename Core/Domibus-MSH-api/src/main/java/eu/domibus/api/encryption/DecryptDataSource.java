
package eu.domibus.api.encryption;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataSource;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class DecryptDataSource implements DataSource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DecryptDataSource.class);
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    protected final DataSource source;
    protected Cipher cipher;

    public DecryptDataSource(final DataSource source, final Cipher cipher) {
        this.source = source;
        this.cipher = cipher;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new CipherInputStream(source.getInputStream(), cipher);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        String contentType = source.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            contentType = APPLICATION_OCTET_STREAM;
            LOG.debug("Original datasource content type is empty, using [{}]", APPLICATION_OCTET_STREAM);
        }
        LOG.trace("Using content type [{}]", contentType);
        return contentType;
    }

    @Override
    public String getName() {
        return "decryptDataSource - " + source.getName();
    }
}
