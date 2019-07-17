package eu.domibus.api.multitenancy;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Wrapper for the Runnable class to be executed. Attempts to lock the file and in case it succeeds it runs the wrapped Runnable
 *
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class SetLockOnFileRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetLockOnFileRunnable.class);

    protected File lockFile;
    protected Runnable runnable;

    public SetLockOnFileRunnable(Runnable runnable, File lockFile) {
        this.runnable = runnable;
        this.lockFile = lockFile;
    }

    @Override
    public void run() {
        // firstly try to lock the file
        // if this fails, it means that another process has an explicit lock on the file
        try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
             FileChannel fileChannel = raf.getChannel();
             FileLock lock = fileChannel.tryLock()) {
            if (lock == null) {
                LOG.debug("Could not acquire lock on file [{}] ", lockFile);
                return;
            }

            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");

        } catch (OverlappingFileLockException e) {
            LOG.debug("Could not acquire lock on file [{}]. File is already locked in this thread or virtual machine", lockFile, e);
        } catch (Exception e) {
            throw new DomainTaskException(String.format("Could not acquire lock on file [%s]", lockFile), e);
        }
    }
}
