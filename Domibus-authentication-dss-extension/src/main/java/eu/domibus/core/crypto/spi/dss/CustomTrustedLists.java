package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.tsl.OtherTrustedList;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class CustomTrustedLists {

    private List<OtherTrustedList> otherTrustedLists;

    public CustomTrustedLists(List<OtherTrustedList> otherTrustedLists) {
        this.otherTrustedLists = otherTrustedLists;
    }

    public List<OtherTrustedList> getOtherTrustedLists() {
        return otherTrustedLists;
    }
}
