package eu.domibus.plugin.fs;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * Helper to convert between files, MIME types and extensions
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 * @author Cosmin Baciu
 */
public class FSMimeTypeHelperImpl implements FSMimeTypeHelper {
    
    private static final Tika TIKA = new Tika();
    private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();

    /**
     * Detects the MIME type of a document with the given file name.
     * @param fileName the file name of the document
     * @return detected MIME type
     */
    @Override
    public String getMimeType(String fileName) {
        return TIKA.detect(fileName);
    }

    /**
     * Returns the preferred file extension for the given MIME type, or an empty
     * string if no extensions are known.
     * @param mimeString the MIME type
     * @return preferred file extension or empty string
     * @throws MimeTypeException if the given media type name is invalid
     */
    @Override
    public String getExtension(String mimeString) throws MimeTypeException {
        MimeType mimeType = MIME_TYPES.forName(mimeString);

        return mimeType.getExtension();
    }

}
