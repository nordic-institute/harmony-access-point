package eu.domibus.plugin.fs.custom;

import javax.persistence.*;


@Entity
@Table(name = "TB_FS_DOMAIN1")
@NamedQueries({
        @NamedQuery(name = "Domain1Entity.findAll",
                query = "select attempt from Domain1Entity attempt")
})
public class Domain1Entity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
