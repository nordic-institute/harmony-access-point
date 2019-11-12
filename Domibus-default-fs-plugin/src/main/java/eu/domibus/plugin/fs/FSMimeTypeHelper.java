package eu.domibus.plugin.fs;

import org.apache.tika.mime.MimeTypeException;

/**
 * @author Cosmin Baciu
 */
public interface FSMimeTypeHelper {

    String getMimeType(String fileName);

    String getExtension(String mimeString) throws MimeTypeException;
}
