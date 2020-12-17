package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.property.listeners.BlacklistChangeListener;
import eu.domibus.core.property.listeners.ConcurrencyChangeListener;
import eu.domibus.core.property.listeners.CronExpressionChangeListener;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(JMockit.class)
public class DomibusPropertyChangeNotifierImplTest {

    @Tested
    DomibusPropertyChangeNotifierImpl domibusPropertyChangeNotifier;

    @Injectable
    List<DomibusPropertyChangeListener> propertyChangeListeners;

    @Injectable
    List<PluginPropertyChangeListener> pluginPropertyChangeListeners;

    @Injectable
    SignalService signalService;

    @Mocked
    BlacklistChangeListener blacklistChangeListener;

    @Mocked
    ConcurrencyChangeListener concurrencyChangeListener;

    @Mocked
    CronExpressionChangeListener cronExpressionChangeListener;

    @Test
    public void signalPropertyValueChanged() {
        String domainCode = "domain1";
        String propertyName = "prop1";
        String propertyValue = "val";
        boolean broadcast = true;

        domibusPropertyChangeNotifier.allPropertyChangeListeners = Arrays.asList(
                blacklistChangeListener,
                concurrencyChangeListener,
                cronExpressionChangeListener
        );

        new Expectations() {{
            blacklistChangeListener.handlesProperty(propertyName);
            result = false;
            concurrencyChangeListener.handlesProperty(propertyName);
            result = true;
            cronExpressionChangeListener.handlesProperty(propertyName);
            result = false;
        }};

        domibusPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);

        new Verifications() {{
            concurrencyChangeListener.propertyValueChanged(domainCode, propertyName, propertyValue);
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void signalPropertyValueChanged_error() {
        String domainCode = "domain1";
        String propertyName = "prop1";
        String propertyValue = "val";
        boolean broadcast = true;

        domibusPropertyChangeNotifier.allPropertyChangeListeners = Arrays.asList(blacklistChangeListener);

        new Expectations() {{
            blacklistChangeListener.handlesProperty(propertyName);
            result = true;
            blacklistChangeListener.propertyValueChanged(domainCode, propertyName, propertyValue);
            result = new Exception("");
        }};

        domibusPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);
        Assert.fail();

        new Verifications() {{
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void signalPropertyValueChanged_error2() {
        String domainCode = "domain1";
        String propertyName = "prop1";
        String propertyValue = "val";
        boolean broadcast = true;

        domibusPropertyChangeNotifier.allPropertyChangeListeners = Arrays.asList(blacklistChangeListener);

        new Expectations() {{
            blacklistChangeListener.handlesProperty(propertyName);
            result = true;
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
            result = new Exception("");
        }};

        domibusPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);
        Assert.fail();

        new Verifications() {{
            blacklistChangeListener.handlesProperty(propertyName);
        }};
    }
}