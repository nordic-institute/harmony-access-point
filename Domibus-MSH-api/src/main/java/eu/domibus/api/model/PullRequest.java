package eu.domibus.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class PullRequest {

    @Column(name = "PULL_REQUEST_MPC")
    protected String mpc;

    public String getMpc() {
        return this.mpc;
    }

    public void setMpc(final String value) {
        this.mpc = value;
    }
}
