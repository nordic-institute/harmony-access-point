package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;


public class EArchiveException extends RuntimeException {

    private String batchId;
    private Long batchEntityId;
    private EArchiveBatchStatus batchStatus;

    public EArchiveException(String batchId, Long batchEntityId, EArchiveBatchStatus batchStatus, Throwable e) {
        super(e);
        this.batchId = batchId;
        this.batchEntityId = batchEntityId;
        this.batchStatus = batchStatus;
    }

    public String getBatchId() {
        return batchId;
    }

    public Long getBatchEntityId() {
        return batchEntityId;
    }

    public EArchiveBatchStatus getBatchStatus() {
        return batchStatus;
    }
}
