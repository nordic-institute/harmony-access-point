package eu.domibus.common.dao;

import eu.domibus.core.party.PartyDao;
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
        PartyDao partyDao = new PartyDao();
        return partyDao;
    }
}
