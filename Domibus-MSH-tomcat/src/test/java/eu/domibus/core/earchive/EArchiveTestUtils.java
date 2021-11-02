package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchStatus;

import java.util.Date;

public class EArchiveTestUtils {

    public static EArchiveBatchEntity createEArchiveBatchEntity(final String batchIdq,
                                                                final RequestType requestType,
                                                                final EArchiveBatchStatus eArchiveBatchStatus,
                                                                final Date dateRequested,
                                                                final Long firstPkUserMessage,
                                                                final Long lastPkUserMessage,
                                                                final Integer batchSize,
                                                                final String storageLocation,
                                                                final String rawJson
                                                                ) {
        EArchiveBatchEntity instance = new EArchiveBatchEntity();
        instance.setBatchId(batchIdq);
        instance.setRequestType(requestType);
        instance.seteArchiveBatchStatus(eArchiveBatchStatus);
        instance.setDateRequested(dateRequested);
        instance.setFirstPkUserMessage(firstPkUserMessage);
        instance.setLastPkUserMessage(lastPkUserMessage);
        instance.setBatchSize(batchSize);
        instance.setStorageLocation(storageLocation);
        instance.setMessageIdsJson(rawJson);
        return instance;
    }
}

