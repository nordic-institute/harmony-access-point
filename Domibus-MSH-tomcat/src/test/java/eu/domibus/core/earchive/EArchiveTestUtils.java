package eu.domibus.core.earchive;

import java.util.Date;

public class EArchiveTestUtils {

    public static EArchiveBatchEntity createEArchiveBatchEntity(final String batchIdq,
                                                                RequestType requestType,
                                                                EArchiveBatchStatus eArchiveBatchStatus,
                                                                Date dateRequested,
                                                                Long lastPkUserMessage,
                                                                Integer batchSize,
                                                                String storageLocation,
                                                                final String rawJson) {
        EArchiveBatchEntity instance = new EArchiveBatchEntity();
        instance.setBatchId(batchIdq);
        instance.setRequestType(requestType);
        instance.seteArchiveBatchStatus(eArchiveBatchStatus);
        instance.setDateRequested(dateRequested);
        instance.setLastPkUserMessage(lastPkUserMessage);
        instance.setBatchSize(batchSize);
        instance.setStorageLocation(storageLocation);
        instance.setMessageIdsJson(rawJson);
        return instance;
    }
}

