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

    public DomibusIPFile(InputStream file, String renameTo) {
        super(Paths.get(""), renameTo);
        this.file = file;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }
}
