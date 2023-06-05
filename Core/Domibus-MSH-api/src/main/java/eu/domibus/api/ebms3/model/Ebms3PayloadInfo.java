package eu.domibus.api.ebms3.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * Each PayloadInfo element identifies payload data associated with the message. The purpose of the
 * PayloadInfo is:
 * • to make it easier to extract particular payload parts associated with this ebMS Message,
 * • and to allow an application to determine whether it can process these payload parts, without
 * having to parse them.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PayloadInfo", propOrder = "partInfo")
public class Ebms3PayloadInfo {

    @XmlElement(name = "PartInfo", required = true)
    protected List<Ebms3PartInfo> partInfo;

    /**
     * This element occurs zero or more times. The PartInfo element is used to reference a MIME
     * attachment, an XML element within the SOAP Body, or another resource which may be obtained
     * by resolving a URL, according to the value of the href attribute
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the partInfo property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartInfo().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ebms3PartInfo }
     *
     * @return a reference to the live list of part info
     */

    //TODO: support payloadreference?
    public List<Ebms3PartInfo> getPartInfo() {
        if (this.partInfo == null) {
            this.partInfo = new ArrayList<>();
        }
        return this.partInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Ebms3PayloadInfo that = (Ebms3PayloadInfo) o;

        return new EqualsBuilder()
                .append(partInfo, that.partInfo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(partInfo)
                .toHashCode();
    }
}
