package eu.domibus.api.model;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.ebms3.model.Ebms3Property;
import eu.domibus.api.encryption.DecryptDataSource;
import eu.domibus.api.message.compression.DecompressionDataSource;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.crypto.Cipher;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries({
        @NamedQuery(name = "PartInfo.findPartInfos", query = "select distinct pi from PartInfo pi left join fetch pi.partProperties where pi.userMessage.entityId=:ENTITY_ID order by pi.partOrder"),
        @NamedQuery(name = "PartInfo.findFilenames", query = "select pi.fileName from PartInfo pi where pi.userMessage.messageId IN :MESSAGEIDS and pi.fileName is not null"),
        @NamedQuery(name = "PartInfo.emptyPayloads", query = "update PartInfo p set p.binaryData = null where p in :PARTINFOS"),
})
@Entity
@Table(name = "TB_PART_INFO")
public class PartInfo extends AbstractBaseEntity implements Comparable<PartInfo> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfo.class);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_MESSAGE_ID_FK")
    protected UserMessage userMessage;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PART_PROPERTIES",
            joinColumns = @JoinColumn(name = "PART_INFO_ID_FK"),
            inverseJoinColumns = @JoinColumn(name = "PART_INFO_PROPERTY_FK")
    )
    protected Set<PartProperty> partProperties; //NOSONAR

    @Embedded
    protected Description description; //NOSONAR

    @Column(name = "HREF")
    protected String href;

    @Lob
    @Column(name = "BINARY_DATA")
    @Basic(fetch = FetchType.EAGER)
    protected byte[] binaryData;

    @Column(name = "FILENAME")
    protected String fileName;

    @Column(name = "IN_BODY")
    protected boolean inBody;

    @Transient
    protected DataHandler payloadDatahandler; //NOSONAR

    @Column(name = "MIME")
    private String mime;

    @Transient
    private long length = -1;

    @Column(name = "PART_ORDER")
    private int partOrder = 0;

    @Column(name = "ENCRYPTED")
    protected Boolean encrypted;

    @Column(name = "COMPRESSED")
    protected Boolean compressed;

    @Transient
    public String getMimeProperty() {
        return partProperties.stream()
                .filter(partProperty -> partProperty.getName().equals(Ebms3Property.MIME_TYPE))
                .filter(Objects::nonNull)
                .findFirst()
                .map(Property::getValue)
                .orElse(null);
    }

    @Transient
    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

    @Transient
    public void setPayloadDatahandler(final DataHandler payloadDatahandler) {
        this.payloadDatahandler = payloadDatahandler;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(final String mime) {
        this.mime = mime;
    }

    public boolean isInBody() {
        return this.inBody;
    }

    public void setInBody(final boolean inBody) {
        this.inBody = inBody;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEncrypted() {
        return BooleanUtils.toBoolean(encrypted);
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Boolean getCompressed() {
        return BooleanUtils.toBoolean(compressed);
    }

    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    @Transient
    public Set<PartProperty> getPartProperties() {
        return partProperties;
    }

    @Transient
    public void setPartProperties(Set<PartProperty> partProperties) {
        this.partProperties = partProperties;
    }

    @PostLoad
    public void loadBinary() {
        if (fileName != null) { /* Create payload data handler from File */
            LOG.debug("LoadBinary from file: " + fileName);
            DataSource fsDataSource = new AutoCloseFileDataSource(fileName);

            if (isEncrypted()) {
                LOG.debug("Using DecryptDataSource for payload [{}]", href);
                final Cipher decryptCipher = getDecryptCipher();
                fsDataSource = new DecryptDataSource(fsDataSource, decryptCipher);
            }

            if(getCompressed()) {
                LOG.debug("Setting the decompressing handler on the the payload [{}]", href);
                fsDataSource = new DecompressionDataSource(fsDataSource, getMime());
            }

            payloadDatahandler = new DataHandler(fsDataSource);
            return;
        }
        /* Create payload data handler from binaryData (byte[]) */
        if (binaryData == null) {
            LOG.debug("Payload is empty!");
            payloadDatahandler = null;
        } else {
            DataSource dataSource = new ByteArrayDataSource(binaryData, mime);

            if (isEncrypted()) {
                LOG.debug("Using DecryptDataSource for payload [{}]", href);
                final Cipher decryptCipher = getDecryptCipher();
                dataSource = new DecryptDataSource(dataSource, decryptCipher);
            }

            if(getCompressed()) {
                LOG.debug("Setting the decompressing handler on the the payload [{}]", href);
                dataSource = new DecompressionDataSource(dataSource, getMime());
            }
            payloadDatahandler = new DataHandler(dataSource);
        }

    }

    @Transient
    protected Cipher getDecryptCipher() {
        LOG.debug("Getting decrypt cipher for payload [{}]", href);
        final PayloadEncryptionService encryptionService = SpringContextProvider.getApplicationContext().getBean("EncryptionServiceImpl", PayloadEncryptionService.class);
        return encryptionService.getDecryptCipherForPayload();
    }

    public Description getDescription() {
        return this.description;
    }

    public void setDescription(final Description value) {
        this.description = value;
    }

    public String getHref() {
        return this.href;
    }

    public void setHref(final String value) {
        this.href = value;
    }

    @Transient
    public long getLength() {
        return length;
    }

    @Transient
    public void setLength(long length) {
        this.length = length;
    }

    public void setPartOrder(int partOrder) {
        this.partOrder = partOrder;
    }

    public int getPartOrder() {
        return partOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("description", description)
                .append("partProperties", partProperties)
                .append("href", href)
                .append("binaryData", binaryData)
                .append("fileName", fileName)
                .append("inBody", inBody)
                .append("payloadDatahandler", payloadDatahandler)
                .append("mime", mime)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartInfo partInfo = (PartInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(description, partInfo.description)
                //.append(partProperties, partInfo.partProperties)
                .append(href, partInfo.href)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(description)
                // .append(partProperties)
                .append(href)
                .toHashCode();
    }

    @Override
    public int compareTo(final PartInfo o) {
        return this.hashCode() - o.hashCode();
    }
}
