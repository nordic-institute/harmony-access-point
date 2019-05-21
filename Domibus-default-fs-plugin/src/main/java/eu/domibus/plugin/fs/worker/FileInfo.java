package eu.domibus.plugin.fs.worker;

public class FileInfo {
    public long size;
    public long modified;
    public String domain;

    public FileInfo(long size, long modified, String domain) {
        this.size = size;
        this.modified = modified;
        this.domain = domain;
    }
}
