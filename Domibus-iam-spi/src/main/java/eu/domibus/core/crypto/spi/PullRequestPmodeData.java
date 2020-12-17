package eu.domibus.core.crypto.spi;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PullRequestPmodeData {

    private final String mpcName;

    public PullRequestPmodeData(String mpcName) {
        this.mpcName = mpcName;
    }

    public String getMpcName() {
        return mpcName;
    }
}
