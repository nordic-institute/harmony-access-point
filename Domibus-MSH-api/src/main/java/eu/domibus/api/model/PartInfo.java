package eu.domibus.api.model;

import eu.domibus.api.datasource.AutoCloseFileDataSource;
import eu.domibus.api.encryption.DecryptDataSource;
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

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries(@NamedQuery(name = "PartInfo.loadBinaryData", query = "select pi.binaryData from PartInfo pi where pi.entityId=:ENTITY_ID"))
@Entity
@Table(name = "TB_PART_INFO")
public class PartInfo extends AbstractBaseEntity implements Comparable<PartInfo> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfo.class);

    @Embedded
    protected Schema schema; //NOSONAR

    @Embedded
    protected Description description; //NOSONAR

    @Embedded
    protected PartProperties partProperties; //NOSONAR

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

    @Column(name = "PART_ORDER", nullable = false)
    private int partOrder = 0;

    @Column(name = "ENCRYPTED")
    protected Boolean encrypted;

    public DataHandler getPayloadDatahandler() {
        return payloadDatahandler;
    }

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

    @PostLoad
    private void loadBinaray() {
        if (fileName != null) { /* Create payload data handler from File */
            LOG.debug("LoadBinary from file: " + fileName);
            DataSource fsDataSource = new AutoCloseFileDataSource(fileName);
            if (isEncrypted()) {
                LOG.debug("Using DecryptDataSource for payload [{}]", href);
                final Cipher decryptCipher = getDecryptCipher();
                fsDataSource = new DecryptDataSource(fsDataSource, decryptCipher);
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
            payloadDatahandler = new DataHandler(dataSource);
        }

    }

    protected Cipher getDecryptCipher() {
        LOG.debug("Getting decrypt cipher for payload [{}]", href);
        final PayloadEncryptionService encryptionService = SpringContextProvider.getApplicationContext().getBean("EncryptionServiceImpl", PayloadEncryptionService.class);
        return encryptionService.getDecryptCipherForPayload();
    }

    public Schema getSchema() {
        return this.schema;
    }

    public void setSchema(final Schema value) {
        this.schema = value;
    }

    public Description getDescription() {
        return this.description;
    }

    public void setDescription(final Description value) {
        this.description = value;
    }

    public PartProperties getPartProperties() {
        return this.partProperties;
    }

    public void setPartProperties(final PartProperties value) {
        this.partProperties = value;
    }

    public String getHref() {
        return this.href;
    }

    public void setHref(final String value) {
        this.href = value;
    }

    public long getLength() {
        return length;
    }

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
                .append("schema", schema)
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
                .append(schema, partInfo.schema)
                .append(description, partInfo.description)
                //.append(partProperties, partInfo.partProperties)
                .append(href, partInfo.href)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(schema)
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
