package eu.domibus.plugin.fs.custom;

import javax.persistence.*;


@Entity
@Table(name = "TB_FS_DOMAIN2")
@NamedQueries({
        @NamedQuery(name = "Domain2Entity.findAll",
                query = "select attempt from Domain2Entity attempt")
})
public class Domain2Entity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
