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
    private boolean writeCheckSum;

    public DomibusIPFile(InputStream file, String renameTo, boolean writeCheckSum) {
        this(file, renameTo);
        this.writeCheckSum = writeCheckSum;
    }

    public DomibusIPFile(InputStream file, String renameTo) {
        super(Paths.get(""), renameTo);
        this.file = file;
        writeCheckSum = true;
    }

    public InputStream getInputStream() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    public boolean writeCheckSum() {
        return writeCheckSum;
    }

    public void setWriteCheckSum(boolean writeCheckSum) {
        this.writeCheckSum = writeCheckSum;
    }
}
