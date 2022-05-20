package eu.domibus.core.csv;

import eu.domibus.api.routing.RoutingCriteria;

/**
 * @author François Gautier
 * @since 4.2
 */
public class MessageFilterCSV {

    private String plugin;
    private RoutingCriteria from;
    private RoutingCriteria to;
    private RoutingCriteria action;
    private RoutingCriteria service;
    private boolean persisted;


    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public RoutingCriteria getFrom() {
        return from;
    }

    public void setFrom(RoutingCriteria from) {
        this.from = from;
    }

    public RoutingCriteria getTo() {
        return to;
    }

    public void setTo(RoutingCriteria to) {
        this.to = to;
    }

    public RoutingCriteria getAction() {
        return action;
    }

    public void setAction(RoutingCriteria action) {
        this.action = action;
    }

    public RoutingCriteria getService() {
        return service;
    }

    public void setService(RoutingCriteria service) {
        this.service = service;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}
