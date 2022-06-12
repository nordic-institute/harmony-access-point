package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 */
@RunWith(JMockit.class)
public class PropertyProviderHelperTest {

    @Tested
    PropertyProviderHelper propertyProviderHelper;

    @Injectable
    ConfigurableEnvironment environment;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

    @Test
    public void filterPropertiesName(@Injectable PropertySource propertySource,
                                     @Injectable Predicate<String> predicate) {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(propertySource);

        new Expectations(propertyProviderHelper) {{
            environment.getPropertySources();
            result = propertySources;

            propertyProviderHelper.filterPropertySource((Predicate<String>) any, (PropertySource) any);
        }};

        propertyProviderHelper.filterPropertyNames(predicate);

        new Verifications() {{
            propertyProviderHelper.filterPropertySource(predicate, propertySource);
        }};
    }

}
