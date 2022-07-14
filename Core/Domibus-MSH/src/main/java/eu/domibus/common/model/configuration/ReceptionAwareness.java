package eu.domibus.common.model.configuration;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.core.ebms3.sender.retry.RetryStrategy;
import eu.domibus.api.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="retry" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="duplicateDetection" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
@Table(name = "TB_PM_RECEPTION_AWARENESS")
public class ReceptionAwareness extends AbstractBaseEntity {

    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;

    @XmlAttribute(name = "retry")
    @Transient
    protected String retryXml;

    @XmlTransient
    @Column(name = "RETRY_TIMEOUT")
    protected int retryTimeout;

    @XmlTransient
    @Column(name = "RETRY_COUNT")
    protected int retryCount;

    @Transient
    @XmlTransient
    private List<Integer> retryIntervals;

    @XmlTransient
    @Column(name = "INITIAL_INTERVAL")
    protected int initialInterval;

    @XmlTransient
    @Column(name = "MULTIPLYING_FACTOR")
    protected int multiplyingFactor;

    @XmlTransient
    @Column(name = "STRATEGY")
    @Enumerated(EnumType.STRING)
    protected RetryStrategy strategy = RetryStrategy.SEND_ONCE;

    @XmlAttribute(name = "duplicateDetection")
    @Column(name = "DUPLICATE_DETECTION")
    protected boolean duplicateDetection;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the duplicateDetection property.
     *
     * @return possible object is
     * {@link String }
     */
    public boolean getDuplicateDetection() {
        return this.duplicateDetection;
    }

    /**
     * Sets the value of the duplicateDetection property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDuplicateDetection(final boolean value) {
        this.duplicateDetection = value;
    }

    public RetryStrategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(final RetryStrategy strategy) {
        this.strategy = strategy;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(final int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryTimeout() {
         return this.retryTimeout;
    }

    public void setRetryTimeout(final int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public int getMultiplyingFactor() {
        return multiplyingFactor;
    }

    public void setMultiplyingFactor(int multiplyingFactor) {
        this.multiplyingFactor = multiplyingFactor;
    }

    public int getInitialInterval() {
        return initialInterval;
    }

    public void setInitialInterval(int initialInterval) {
        this.initialInterval = initialInterval;
    }

    public List<Integer> getRetryIntervals() {
        if (retryIntervals==null) {
            retryIntervals = calculateRetryIntervals(this.initialInterval, this.multiplyingFactor, this.retryTimeout);
        }
        return retryIntervals;
    }

    public void setRetryIntervals(List<Integer> retryIntervals) {
        this.retryIntervals = retryIntervals;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceptionAwareness)) return false;
        if (!super.equals(o)) return false;

        final ReceptionAwareness that = (ReceptionAwareness) o;

        if (!name.equalsIgnoreCase(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @SuppressWarnings("unused")
    public void init(final Configuration configuration) {
        try {
            if (this.retryXml != null) {
                final String[] retryValues = this.retryXml.split(";");
                this.retryTimeout = Integer.parseInt(retryValues[0]);
                if (retryValues.length==4 && "PROGRESSIVE".equals(retryValues[3])) {
                    this.initialInterval = Integer.parseInt(retryValues[1]);
                    this.multiplyingFactor = Integer.parseInt(retryValues[2]);
                    this.strategy = RetryStrategy.valueOf(retryValues[3]);
                    this.retryIntervals = calculateRetryIntervals(this.initialInterval, this.multiplyingFactor, this.retryTimeout);
                    this.retryCount = this.retryIntervals.size()-1;
                    return;
                }
                this.retryCount = Integer.parseInt(retryValues[1]);

                this.strategy = RetryStrategy.valueOf(retryValues[2]);
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_003,
                    "The format of the receptionAwareness.retry is incorrect :[" + retryXml + "]. " +
                            "Format: \"retryTimeout;retryCount;(CONSTANT - SEND_ONCE)\" (ex: 4;12;CONSTANT)", e);
        }

    }

    /**
     * Calculates the list of retry intervals in a progressive strategy. Examples:
     * (initialInterval,multiplyingFactor,timeout)=(1,2,9) => (1,2,4,8)
     * (1,3,100) => [1,3,9,27,81]
     * (2,3,100) => [2,6,18,54]
     * (20,3,100) => [20,60]
     * (3,2,100) => [3,6,12,24,48,96]
     * @param initialInterval - the first retry interval
     * @param multiplyingFactor - the next retry interval will be the current multiplied by this factor
     * @param timeout - the maximum time interval for retrials since the first one
     * @return
     */
    private List calculateRetryIntervals(int initialInterval, int multiplyingFactor, int timeout) {
        List result = new ArrayList();
        int x = 0;
        int crtTriggerTime = initialInterval;
        while (crtTriggerTime <= timeout) {
            result.add(crtTriggerTime);
            crtTriggerTime = crtTriggerTime * multiplyingFactor;
            x++;
        }
        return result;
    }


}
