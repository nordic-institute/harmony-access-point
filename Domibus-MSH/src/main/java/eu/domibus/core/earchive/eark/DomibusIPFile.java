package eu.domibus.core.earchive.eark;

import org.roda_project.commons_ip2.model.IPFile;

import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author François Gautier
 * @since 5.0
 */
public class DomibusIPFile extends IPFile {

    private transient InputStream file;
    private boolean writeChecksum;

    public DomibusIPFile(InputStream file, String renameTo, boolean writeChecksum) {
        this(file, renameTo);
        this.writeChecksum = writeChecksum;
    }

    public DomibusIPFile(InputStream file, String renameTo) {
        super(Paths.get(""), renameTo);
        this.file = file;
        writeChecksum = true;
    }

    public InputStream getInputStream() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    public boolean writeChecksum() {
        return writeChecksum;
    }

    public void setWriteChecksum(boolean writeChecksum) {
        this.writeChecksum = writeChecksum;
    }
}
