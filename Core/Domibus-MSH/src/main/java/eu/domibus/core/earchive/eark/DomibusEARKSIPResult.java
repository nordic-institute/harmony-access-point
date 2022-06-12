package eu.domibus.core.earchive.eark;

import java.nio.file.Path;

/**
 * @author François Gautier
 * @since 5.0
 */
public class DomibusEARKSIPResult {

    private Path directory;

    private String manifestChecksum;

    public DomibusEARKSIPResult(Path directory, String manifestChecksum) {
        this.directory = directory;
        this.manifestChecksum = manifestChecksum;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public String getManifestChecksum() {
        return manifestChecksum;
    }

    public void setManifestChecksum(String manifestChecksum) {
        this.manifestChecksum = manifestChecksum;
    }
}
