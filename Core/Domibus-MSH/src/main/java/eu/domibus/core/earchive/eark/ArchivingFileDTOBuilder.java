package eu.domibus.core.earchive.eark;

import java.io.InputStream;

public class ArchivingFileDTOBuilder {
    private InputStream inputStream;
    private Long size;
    private String mimeType;

    public static ArchivingFileDTOBuilder getInstance(){
        return new ArchivingFileDTOBuilder();
    }

    public ArchivingFileDTOBuilder setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public ArchivingFileDTOBuilder setSize(Long size) {
        this.size = size;
        return this;
    }

    public ArchivingFileDTOBuilder setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public ArchivingFileDTO build() {
        return new ArchivingFileDTO(inputStream, size, mimeType);
    }
}