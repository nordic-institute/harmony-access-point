package eu.domibus.api.ebms3.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * This OPTIONAL element
 * occurs at most once, and contains message properties that are user-specific. As parts of the
 * header such properties allow for more efficient monitoring, correlating, dispatching and validating
 * functions (even if these are out of scope of ebMS specification) which would otherwise require
 * payload access.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageProperties", propOrder = "property")
public class Ebms3MessageProperties {

    @XmlElement(name = "Property", required = true)
    protected Set<Ebms3Property> property;

    /**
     * An eb:Property element is of xs:anySimpleType (e.g. string, URI) and has two attributes:
     * • @name: The value of this REQUIRED attribute must be agreed upon between partners.
     * • @type: This OPTIONAL attribute allows for resolution of conflicts between properties with the
     * same name, and may also help with Property grouping, e.g. various elements of an address.
     * Its actual semantics is beyond the scope of this specification. The element is intended to be consumed
     * outside the ebMS-specified functions. It may contain some information that qualifies or abstracts message
     * data, or that allows for binding the message to some business process. A representation in the header of
     * such properties allows for more efficient monitoring, correlating, dispatching and validating functions (even
     * if these are out of scope of ebMS specification) that do not require payload access.
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the property property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ebms3Property }
     *
     * @return a reference to the live list of properties
     */
    public Set<Ebms3Property> getProperty() {
        if (this.property == null) {
            this.property = new HashSet<>();
        }
        return this.property;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Ebms3MessageProperties)) return false;

        final Ebms3MessageProperties that = (Ebms3MessageProperties) o;

        return !(this.property != null ? !this.property.equals(that.property) : that.property != null);

    }

    @Override
    public int hashCode() {
        return this.property != null ? this.property.hashCode() : 0;
    }
}
