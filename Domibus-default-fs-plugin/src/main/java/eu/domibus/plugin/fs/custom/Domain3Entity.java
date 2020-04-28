package eu.domibus.plugin.fs.custom;

import javax.persistence.*;


@Entity
@Table(name = "TB_FS_DOMAIN3")
@NamedQueries({
        @NamedQuery(name = "Domain3Entity.findAll",
                query = "select attempt from Domain3Entity attempt")
})
public class Domain3Entity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
