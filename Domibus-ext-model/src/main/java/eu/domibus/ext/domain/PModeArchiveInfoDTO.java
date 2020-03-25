package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for PMode download
 * It stores information related to PMode files
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class PModeArchiveInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id of the PMode file */
    private int id;

    /** configuraiton date */
    private Date configurationDate;

    /** username who has save/update the PMode file */
    private String username;

    /** description when the PMode file is created/updated */
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("configurationDate", configurationDate)
                .append("username", username)
                .append("description", description)
                .toString();
    }
}
