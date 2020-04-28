package eu.domibus.plugin.fs.custom;

import javax.persistence.*;


@Entity
@Table(name = "TB_FS_DOMAIN4")
@NamedQueries({
        @NamedQuery(name = "Domain4Entity.findAll",
                query = "select attempt from Domain4Entity attempt")
})
public class Domain4Entity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
