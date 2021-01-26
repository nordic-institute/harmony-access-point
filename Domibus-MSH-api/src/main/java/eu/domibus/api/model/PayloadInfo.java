package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class PayloadInfo {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "PAYLOADINFO_ID")
    @OrderColumn(name="PART_ORDER")
    protected List<PartInfo> partInfo;

    public List<PartInfo> getPartInfo() {
        if (this.partInfo == null) {
            this.partInfo = new ArrayList<>();
        }
        return this.partInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PayloadInfo that = (PayloadInfo) o;

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
