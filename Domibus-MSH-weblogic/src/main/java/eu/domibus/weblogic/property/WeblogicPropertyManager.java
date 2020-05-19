package eu.domibus.weblogic.property;

import eu.domibus.ext.domain.Module;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the Weblogic specific properties.
 */
@Service//("serverPropertyManager")
public class WeblogicPropertyManager extends WeblogicCommonPropertyManager {
    public WeblogicPropertyManager() {
        super(Module.WEBLOGIC);
    }
}
