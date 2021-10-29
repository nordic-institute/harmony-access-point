package eu.domibus.plugin.fs;

import eu.domibus.ext.services.DefaultDomainsAwareExt;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Service responsible with reacting to adding and removing of domains at runtime
 */
@Service
public class FSPluginDomainsManagerImpl extends DefaultDomainsAwareExt {

    public FSPluginDomainsManagerImpl(FSPluginProperties fsPluginProperties) {
        super(fsPluginProperties);
    }
}
