package eu.domibus.core.alerts.model.web;

import eu.domibus.api.validators.SkipWhiteListed;

import java.util.Date;
import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public class AlertCsvRO {

    private boolean processed;

    private String alertType;

    private String alertLevel;

    private String alertStatus;

    private Date creationTime;

    @SkipWhiteListed
    private List<String> parameters;

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }
}
