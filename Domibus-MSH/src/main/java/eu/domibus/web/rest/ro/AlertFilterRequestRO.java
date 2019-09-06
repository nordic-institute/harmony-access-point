package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class AlertFilterRequestRO implements Serializable {
    private int page = 0;
    private int pageSize = 10;
    private Boolean asc = true;
    private String orderBy;
    private String processed;
    private String alertType;
    private String alertStatus;
    private Long alertId;
    private String alertLevel;
    private String creationFrom;
    private String creationTo;
    private String reportingFrom;
    private String reportingTo;
    private String[] parameters;
    private String dynamicFrom;
    private String dynamicTo;
    private Boolean domainAlerts = false;

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

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getCreationFrom() {
        return creationFrom;
    }

    public void setCreationFrom(String creationFrom) {
        this.creationFrom = creationFrom;
    }

    public String getCreationTo() {
        return creationTo;
    }

    public void setCreationTo(String creationTo) {
        this.creationTo = creationTo;
    }

    public String getReportingFrom() {
        return reportingFrom;
    }

    public void setReportingFrom(String reportingFrom) {
        this.reportingFrom = reportingFrom;
    }

    public String getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(String reportingTo) {
        this.reportingTo = reportingTo;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String getDynamicFrom() {
        return dynamicFrom;
    }

    public void setDynamicFrom(String dynamicFrom) {
        this.dynamicFrom = dynamicFrom;
    }

    public String getDynamicTo() {
        return dynamicTo;
    }

    public void setDynamicTo(String dynamicTo) {
        this.dynamicTo = dynamicTo;
    }

    public Boolean getDomainAlerts() {
        return domainAlerts;
    }

    public void setDomainAlerts(Boolean domainAlerts) {
        this.domainAlerts = domainAlerts;
    }

}
