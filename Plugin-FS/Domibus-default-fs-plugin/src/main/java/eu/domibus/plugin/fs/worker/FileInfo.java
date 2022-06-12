package eu.domibus.plugin.fs.worker;

public class FileInfo {
    private long size;
    private long modified;
    private String domain;

    public FileInfo(long size, long modified, String domain) {
        this.size = size;
        this.modified = modified;
        this.domain = domain;
    }

    public long getSize() {
        return size;
    }

    public long getModified() {
        return modified;
    }

    public String getDomain() {
        return domain;
    }
}
