package eu.domibus.core.pulling;

import javax.persistence.*;


@Entity
@Table(name = "TB_PULL_REQUEST")
@NamedQueries({
        @NamedQuery(name = "PullRequest.count", query = "select count(uuid) from PullRequest"),
        @NamedQuery(name = "PullRequest.delete", query = "delete from PullRequest pr where pr.uuid= :UUID"),
})
public class PullRequest {

    @Id
    @Column(name = "PULL_REQUEST_UUID")
    private String uuid;

    @Column(name = "MPC")
    private String mpc;

    public PullRequest() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }


}
