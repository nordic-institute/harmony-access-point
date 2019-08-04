package eu.domibus.plugin.fs.property;

import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSPluginPropertiesTest {

    @Injectable("fsPluginProperties")
    Properties properties;

    @Injectable
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Injectable
    protected DomainExtService domainExtService;

    @Tested
    FSPluginProperties fsPluginProperties;

    @Test
    public void testReadDomains() {
        Set<String> domains = new HashSet<>();
        domains.add("fsplugin.domains.domain1");
        domains.add("fsplugin.domains.domain2");
        domains.add("fsplugin.domains.domain3");

        new Expectations(Collections.class) {{
            properties.stringPropertyNames();
            result = domains;

            Collections.sort((List) any, (Comparator) any);
        }};

        final List<String> domainsList = fsPluginProperties.readDomains();
        Assert.assertEquals(4, domainsList.size());
        Assert.assertTrue(domainsList.contains(FSSendMessagesService.DEFAULT_DOMAIN));

    }

}
