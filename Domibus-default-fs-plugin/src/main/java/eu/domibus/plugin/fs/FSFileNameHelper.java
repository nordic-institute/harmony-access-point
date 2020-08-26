package eu.domibus.plugin.fs;

import com.sun.javafx.tools.packager.Log;
import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper to create and recognize derived file names
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSFileNameHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSFileNameHelper.class);

    public static final String NAME_SEPARATOR = "_";
    public static final String EXTENSION_SEPARATOR = ".";
    public static final Pattern OUT_DIRECTORY_PATTERN = Pattern.compile("/" + FSFilesManager.OUTGOING_FOLDER + "(/|$)");
    protected static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    protected static final Pattern PROCESSED_FILE_PATTERN = Pattern.compile(
            NAME_SEPARATOR + UUID_PATTERN + "@.", Pattern.CASE_INSENSITIVE);
    public static final String LOCK_SUFFIX = ".lock";

    protected List<String> stateSuffixes;

    public FSFileNameHelper(List<String> stateSuffixes) {
        this.stateSuffixes = stateSuffixes;
    }

    /**
     * Checks if a given file name has been derived from a {@link eu.domibus.common.MessageStatus}.
     * In practice checks if the filename is suffixed by a dot and any of the
     * known {@link eu.domibus.common.MessageStatus}.
     *
     * @param fileName the file name to test
     * @return true, if the file name has been derived from a {@link eu.domibus.common.MessageStatus}
     */
    public boolean isAnyState(final String fileName) {
        return StringUtils.endsWithAny(fileName, stateSuffixes.toArray(new String[0]));
    }

    /**
     * Checks if a given file name has been derived from any message Id.
     * In practice checks if the filename contains an underscore followed by a
     * message Id.
     *
     * @param fileName the file name to test
     * @return true, if the file name has been derived from a message Id
     */
    public boolean isProcessed(final String fileName) {
        return PROCESSED_FILE_PATTERN.matcher(fileName).find();
    }

    /**
     * Checks if a given file name has been derived from a given message Id.
     * In practice checks if the filename contains an underscore followed by the
     * given message Id.
     *
     * @param fileName  the file name to test
     * @param messageId the message Id to test
     * @return true, if the file name has been derived from the given message Id
     */
    public static boolean isMessageRelated(String fileName, String messageId) {
        return fileName.contains(NAME_SEPARATOR + messageId);
    }

    /**
     * Checks if a given file is a lock file, used to lock access to a message
     * file.
     *
     * @param fileName the file name to test
     * @return true, if the file name matches the lock pattern.
     */
    public boolean isLockFile(final String fileName) {
        return StringUtils.endsWith(fileName, LOCK_SUFFIX);
    }

    public String getLockFilename(FileObject file) {
        return file.getName().getBaseName() + LOCK_SUFFIX;
    }

    public String getLockFileName(String fileName) {
        return fileName + LOCK_SUFFIX;
    }

    /**
     * Derives a new file name from the given file name and a message Id.
     * In practice, for a given file name {@code filename.ext} and message Id
     * {@code messageId} generates a new file name of the form {@code filename_messageId.ext}.
     *
     * @param fileName  the file name to derive
     * @param messageId the message Id to use for the derivation
     * @return a new file name of the form {@code filename_messageId.ext}
     */
    public String deriveFileName(final String fileName, final String messageId) {
        int extensionIdx = StringUtils.lastIndexOf(fileName, EXTENSION_SEPARATOR);

        if (extensionIdx != -1) {
            String fileNamePrefix = StringUtils.substring(fileName, 0, extensionIdx);
            String fileNameSuffix = StringUtils.substring(fileName, extensionIdx + 1);

            return fileNamePrefix + NAME_SEPARATOR + messageId + EXTENSION_SEPARATOR + fileNameSuffix;
        } else {
            return fileName + NAME_SEPARATOR + messageId;
        }
    }

    /**
     * Derives a new file name from the given file name and a {@link eu.domibus.common.MessageStatus}.
     * In practice, for a given file name {@code filename.ext} and message status
     * {@code MESSAGE_STATUS} generates a new file name of the form {@code filename.ext.MESSAGE_STATUS}.
     *
     * @param fileName the file name to derive
     * @param status   the message status to use for the derivation
     * @return a new file name of the form {@code filename.ext.MESSAGE_STATUS}
     */
    public String deriveFileName(final String fileName, final MessageStatus status) {
        return stripStatusSuffix(fileName) + EXTENSION_SEPARATOR + status.name();
    }

    public String stripStatusSuffix(final String fileName) {
        String result = fileName;
        if (isAnyState(fileName)) {
            result = StringUtils.substringBeforeLast(fileName, EXTENSION_SEPARATOR);
        }
        return result;
    }

    /**
     * Derives the related file name from the lock file name.
     *
     * @param fileName the lock file name
     * @return the base file name
     */
    public String stripLockSuffix(final String fileName) {
        String result = fileName;
        if (isLockFile(fileName)) {
            result = StringUtils.substringBeforeLast(fileName, LOCK_SUFFIX);
        }
        return result;
    }

    /**
     * The files moved to the SENT directory will be moved either directly under the SENT directory, under the same
     * directory structure as the one it originated from. E.g.: a file originally located in the DOMAIN1/OUT/Invoice
     * directory will be moved to the DOMAIN1/SENT/Invoice directory after it has been sent.
     *
     * @param fileURI the file URI
     * @return the derived sent directory location
     */
    public String deriveSentDirectoryLocation(String fileURI) {
        return deriveDirectoryLocation(fileURI, FSFilesManager.SENT_FOLDER);
    }

    /**
     * The files moved to the FAILED directory (if property fsplugin.messages.failed.action=archive) will be moved
     * either directly under the FAILED under the same directory structure as the one it originated from. E.g.: a file
     * originally located in the DOMAIN1/OUT/Invoice directory will be moved to the DOMAIN1/FAILED/Invoice directory
     * after it has been sent.
     *
     * @param fileURI the file URI
     * @return the derived failed directory location
     */
    public String deriveFailedDirectoryLocation(String fileURI) {
        return deriveDirectoryLocation(fileURI, FSFilesManager.FAILED_FOLDER);
    }

    public List<FileObject> filterLockedFiles(FileObject[] files) {
        return Arrays.stream(files)
                .filter(file -> isLockFile(file.getName().getBaseName()))
                .collect(Collectors.toList());
    }

    protected String deriveDirectoryLocation(String fileURI, String destFolder) {
        Matcher matcher = OUT_DIRECTORY_PATTERN.matcher(fileURI);
        return matcher.replaceFirst("/" + destFolder + "/");
    }

    public Optional<String> getRelativeName(FileObject rootFolder, FileObject f) {
        try {
            return Optional.of(rootFolder.getName().getRelativeName(f.getName()));
        } catch (FileSystemException e) {
            LOG.debug("Exception while trying to get the relative name of [{}] relative to [{}]", f.getName(), rootFolder.getName());
            return Optional.empty();
        }
    }
}
