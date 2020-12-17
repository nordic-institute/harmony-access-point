package eu.domibus.core.party;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public class PartyDaoConfig {

    @Bean
    public PartyDao getPartyDao() {
        return new PartyDao();
    }
}
