package eu.domibus.api.pmode;

import java.util.Date;

public class PModeArchiveInfo {

    private long id;
    private Date configurationDate;
    private String username;
    private String description;

    public PModeArchiveInfo(){}

    public PModeArchiveInfo(long id, Date configurationDate, String username, String description) {
        this.id = id;
        this.configurationDate = configurationDate;
        this.username = username;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getConfigurationDate() {
        return configurationDate;
    }

    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
