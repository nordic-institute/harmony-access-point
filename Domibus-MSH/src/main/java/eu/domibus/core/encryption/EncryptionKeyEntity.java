
package eu.domibus.core.encryption;

import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 * <p>
 * Entity class containing the key used for symmetric encryption
 */
@Entity
@Table(name = "TB_ENCRYPTION_KEY")
@NamedQueries({
        @NamedQuery(name = "EncryptionKeyEntity.findByUsage", query = "FROM EncryptionKeyEntity key where key.usage=:USAGE"),
})
public class EncryptionKeyEntity extends AbstractBaseEntity {

    @Column(name = "KEY_USAGE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private EncryptionUsage usage;

    @Lob
    @Column(name = "SECRET_KEY")
    protected byte[] secretKey;

    @Lob
    @Column(name = "INIT_VECTOR")
    protected byte[] initVector;

    public EncryptionUsage getUsage() {
        return usage;
    }

    public void setUsage(EncryptionUsage usage) {
        this.usage = usage;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public byte[] getInitVector() {
        return initVector;
    }

    public void setInitVector(byte[] initVector) {
        this.initVector = initVector;
    }
}
