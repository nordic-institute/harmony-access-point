package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Party request for listing parties on external API
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class PartyFilterRequestDTO {

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
        this.pageStart = pageStart < 0 ? 0 : pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize < 0 ? Integer.MAX_VALUE : pageSize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("endPoint", endPoint)
                .append("partyId", partyId)
                .append("process", process)
                .append("pageStart", pageStart)
                .append("pageSize", pageSize)
                .toString();
    }
}
