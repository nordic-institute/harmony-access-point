package eu.domibus.web.rest.ro;

import eu.domibus.web.rest.validators.PropsNotBlacklisted;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@PropsNotBlacklisted
public class PartyFilterRequestRO implements Serializable {
    private String name;
    private String endPoint;
    private String partyId;
    private String process;
    private int pageStart = 0;
    private int pageSize = 10;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
