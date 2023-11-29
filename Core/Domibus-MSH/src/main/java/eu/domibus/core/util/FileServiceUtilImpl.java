package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.domibus.api.exceptions.DomibusCoreErrorCode.DOM_001;

/**
 * @author Catalin Enache
 * @since 4.1.4
 */
@Service
public class FileServiceUtilImpl implements FileServiceUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FileServiceUtilImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return fileName;
        }
        final String sanitizedFileName = FilenameUtils.getName(fileName);
        if (StringUtils.isBlank(sanitizedFileName)) {
            LOG.warn("Unable to sanitize file name which has the value [{}]", fileName);
            return null;
        }
        if (!StringUtils.equals(fileName, sanitizedFileName)) {
            LOG.warn("file name value=[{}] will be sanitized to=[{}]", fileName, sanitizedFileName);
            return sanitizedFileName;
        }
        //file name is correct no need to sanitize
        return fileName;
    }

    @Override
    public String getExtension(String mime) {
        try {
            return MimeTypes.getDefaultMimeTypes().forName(mime).getExtension();
        } catch (MimeTypeException e) {
            LOG.warn("Mimetype [{}] not found", mime);
            return "";
        }
    }

    public byte[] getContentFromFile(String location) throws IOException {
        File file = new File(location);
        if (!file.exists()) {
            throw new IOException(String.format("File with the path [%s] does not exist", location));
        }
        Path path = Paths.get(file.getAbsolutePath());
        return Files.readAllBytes(path);
    }

    @Override
    public String URLEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new DomibusCoreException(DOM_001, "Encode string [" + s + "] in error", e);
        }
    }

    /**
     * Based on org.apache.commons.io.FileUtils#copyToFile(java.io.InputStream, java.io.File)
     * Remove the checks performed by the copyToFile method because under heavy load the method blocks on java.io.UnixFileSystem.getBooleanAttributes(UnixFileSystem.java:247)
     * @throws IOException
     */
    @Override
    public int copyToFile(InputStream in, File out) throws IOException {
        FileUtils.forceMkdirParent(out);
        final OutputStream outputStream = new FileOutputStream(out, false);
        try {
            return IOUtils.copy(in, outputStream);
        } finally {
            //close the streams in order
            in.close();
            outputStream.close();
        }
    }
}
