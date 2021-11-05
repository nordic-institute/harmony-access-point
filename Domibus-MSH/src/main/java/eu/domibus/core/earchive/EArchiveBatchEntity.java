package eu.domibus.core.earchive;

import eu.domibus.api.model.UserMessageDTO;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author François Gautier
 * @since 5.0
 */
@Entity
@Table(name = "TB_EARCHIVE_BATCH")
@NamedQuery(name = "EArchiveBatchEntity.findByEntityId", query = "FROM EArchiveBatchEntity batch where batch.entityId = :BATCH_ENTITY_ID")
@NamedQuery(name = "EArchiveBatchEntity.findByBatchId", query = "FROM EArchiveBatchEntity batch where batch.batchId = :BATCH_ID")
@NamedQuery(name = "EArchiveBatchEntity.findLastEntityIdArchived",
        query = "SELECT max(b.lastPkUserMessage) FROM EArchiveBatchEntity b WHERE b.requestType =  :REQUEST_TYPE")

@SqlResultSetMapping(
        name = "EArchiveBatchUserMessageMapping",
        classes = @ConstructorResult(
                targetClass = UserMessageDTO.class,
                columns = {
                        @ColumnResult(name = "FK_USER_MESSAGE_ID", type = Long.class),
                        @ColumnResult(name = "MESSAGE_ID", type = String.class),
                }
        )
)

// UserMessageDTO
@NamedNativeQuery(name = "EArchiveBatchRequest.getMessagesForBatchId",
        resultSetMapping = "EArchiveBatchUserMessageMapping",
        query = "select msgMap.FK_USER_MESSAGE_ID, userMessage.MESSAGE_ID " +
                " FROM TB_EARCHIVE_BATCH batch" +
                " INNER JOIN TB_EARCHIVEBATCH_UM msgMap " +
                "   ON batch.ID_PK = msgMap.FK_EARCHIVE_BATCH_ID " +
                " INNER JOIN TB_USER_MESSAGE userMessage " +
                "   ON msgMap.FK_USER_MESSAGE_ID = userMessage.ID_PK " +
                " WHERE batch.BATCH_ID = :batchId " +
                " ORDER BY msgMap.FK_USER_MESSAGE_ID ASC"
)

public class EArchiveBatchEntity extends EArchiveBatchBaseEntity {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "MESSAGEIDS_JSON")
    protected byte[] messageIdsJson;

    public void setMessageIdsJson(String rawJson) {
        byte[] bytes = rawJson.getBytes(StandardCharsets.UTF_8);
        this.messageIdsJson = bytes;
    }

    public void setMessageIdsJson(byte[] bytes) {
        this.messageIdsJson = bytes;
    }

    public byte[] getMessageIdsJson() {
        return messageIdsJson;
    }

    @Override
    public String toString() {
        return "EArchiveBatchEntity{" +
                "batchId='" + batchId + '\'' +
                ", requestType=" + requestType +
                ", eArchiveBatchStatus=" + eArchiveBatchStatus +
                ", dateRequested=" + dateRequested +
                ", lastPkUserMessage=" + lastPkUserMessage +
                ", batchSize=" + batchSize +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", storageLocation='" + storageLocation + '\'' +
                "} " + super.toString();
    }
}
