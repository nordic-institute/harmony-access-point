package eu.domibus.core.pmode;

import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author François Gautier
 * @since 5.0
 * <p>
 * Update the entities with {@link Propagation#REQUIRES_NEW} to trigger the {@link org.hibernate.envers.Audited}
 */
@Service
public class ConfigurationRawTestService {

    @Autowired
    private ConfigurationRawDAO configurationRawDAO;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createConfigurationRawAudited(ConfigurationRaw configurationRaw) {
        configurationRawDAO.update(configurationRaw);
    }
}
