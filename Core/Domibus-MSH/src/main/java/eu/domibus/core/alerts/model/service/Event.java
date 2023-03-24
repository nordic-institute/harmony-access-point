package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import eu.domibus.core.alerts.model.common.AccountEventKey;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = Event.class)
public class Event {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(Event.class);

    private long entityId;

    private Date reportingTime;

    private EventType type;

    private Map<String, AbstractPropertyValue> properties = new HashMap<>();

    public Event(final EventType type) {
        this.reportingTime = new Date();
        this.type = type;
    }

    public Event() {
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Map<String, AbstractPropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, AbstractPropertyValue> properties) {
        this.properties = properties;
    }

    public Optional<String> findStringProperty(final String key) {
        final StringPropertyValue stringPropertyValue = (StringPropertyValue) properties.get(key);
        if (stringPropertyValue == null) {
            LOG.error("No event property with such key as key[{}]", key);
            throw new IllegalArgumentException("Invalid property key");
        }
        if (stringPropertyValue.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(stringPropertyValue.getValue());
    }

    public Optional<String> findOptionalProperty(final String key) {
        final AbstractPropertyValue property = properties.get(key);
        if (property == null || property.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(property.getValue().toString());
    }

    public void addProperty(final String key, final AbstractPropertyValue abstractProperty) {
        if (abstractProperty instanceof StringPropertyValue) {
            addStringKeyValue(key, ((StringPropertyValue) abstractProperty).getValue());
        } else if (abstractProperty instanceof DatePropertyValue) {
            addDateKeyValue(key, ((DatePropertyValue) abstractProperty).getValue());
        } else {
            LOG.error("Invalid property type: key[{}] type[{}]", key, abstractProperty.getValue().getClass());
            throw new IllegalArgumentException("Invalid property type");
        }
    }

    public void addAccountKeyValue(final AccountEventKey key, final String value) {
        addStringKeyValue(key.name(), value);
    }

    public void addStringKeyValue(final String key, final String value) {
        properties.put(key, new StringPropertyValue(key, value));
    }

    public void addDateKeyValue(final String key, final Date value) {
        properties.put(key, new DatePropertyValue(key, value));
    }


    private LocalDate lastAlertDate;

    public LocalDate getLastAlertDate() {
        return lastAlertDate;
    }

    public void setLastAlertDate(LocalDate lastAlertDate) {
        this.lastAlertDate = lastAlertDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entityId", entityId)
                .append("reportingTime", reportingTime)
                .append("type", type)
                .append("properties", properties)
                .toString();
    }
}
