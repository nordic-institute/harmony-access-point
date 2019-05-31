package eu.domibus.web.rest.ro;



import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class AlertFilterRequestRO implements Serializable {
    private int page = 0;
    private int pageSize = 10;
    private Boolean ask = true;
    private String column;
    private String processed;
    private String alertType;
    private String alertStatus;
    private Integer alertId;
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

    public Boolean getAsk() {
        return ask;
    }

    public void setAsk(Boolean ask) {
        this.ask = ask;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
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

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
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
