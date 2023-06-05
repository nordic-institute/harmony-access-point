package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.tsl.source.TLSource;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class CustomTrustedLists {

    private List<TLSource> otherTrustedLists;

    public CustomTrustedLists(List<TLSource> otherTrustedLists) {
        this.otherTrustedLists = otherTrustedLists;
    }

    public List<TLSource> getOtherTrustedLists() {
        return otherTrustedLists;
    }
}
