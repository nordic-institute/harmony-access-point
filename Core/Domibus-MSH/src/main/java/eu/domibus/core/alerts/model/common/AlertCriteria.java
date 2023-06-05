package eu.domibus.core.alerts.model.common;

import eu.domibus.api.alerts.AlertLevel;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AlertCriteria {

    private int page;

    private int pageSize;

    private Boolean asc;

    private String orderBy;

    private Boolean processed;

    private AlertType alertType;

    private AlertStatus alertStatus;

    private AlertLevel alertLevel;

    private Long alertID;

    private Date creationFrom;

    private Date creationTo;

    private Date reportingFrom;

    private Date reportingTo;

    private Date dynamicaPropertyFrom;

    private Date dynamicaPropertyTo;

    private String uniqueDynamicDateParameter;

    private Map<String, String> parameters = new HashMap<>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getAsc() {
        return asc;
    }

    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public void setProcessed(String processed) {
        if (StringUtils.isNotEmpty(processed)) {
            this.processed = Boolean.valueOf(processed);
        }
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public void setAlertType(String alertType) {
        if (StringUtils.isNotEmpty(alertType)) {
            this.alertType = AlertType.valueOf(alertType);
        }
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(final String alertStatus) {
        if (StringUtils.isNotEmpty(alertStatus)) {
            this.alertStatus = AlertStatus.valueOf(alertStatus);
        }
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        if (StringUtils.isNotEmpty(alertLevel)) {
            this.alertLevel = AlertLevel.valueOf(alertLevel);
        }
    }

    public Long getAlertID() {
        return alertID;
    }

    public void setAlertID(Long alertID) {
        this.alertID = alertID;
    }

    public Date getCreationFrom() {
        return creationFrom;
    }

    public void setCreationFrom(Date creationFrom) {
        this.creationFrom = creationFrom;
    }

    public Date getCreationTo() {
        return creationTo;
    }

    public void setCreationTo(Date creationTo) {
        this.creationTo = creationTo;
    }

    public Date getReportingFrom() {
        return reportingFrom;
    }

    public void setReportingFrom(Date reportingFrom) {
        this.reportingFrom = reportingFrom;
    }

    public Date getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(Date reportingTo) {
        this.reportingTo = reportingTo;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Date getDynamicaPropertyFrom() {
        return dynamicaPropertyFrom;
    }

    public void setDynamicaPropertyFrom(Date dynamicaPropertyFrom) {
        this.dynamicaPropertyFrom = dynamicaPropertyFrom;
    }

    public Date getDynamicaPropertyTo() {
        return dynamicaPropertyTo;
    }

    public void setDynamicaPropertyTo(Date dynamicaPropertyTo) {
        this.dynamicaPropertyTo = dynamicaPropertyTo;
    }

    public String getUniqueDynamicDateParameter() {
        return uniqueDynamicDateParameter;
    }

    public void setUniqueDynamicDateParameter(String uniqueDynamicDateParameter) {
        this.uniqueDynamicDateParameter = uniqueDynamicDateParameter;
    }
}
