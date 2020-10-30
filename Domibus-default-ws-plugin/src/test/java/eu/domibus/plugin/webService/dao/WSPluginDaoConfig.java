package eu.domibus.plugin.webService.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WSPluginDaoConfig {

    @Bean
    public WSBackendMessageLogDao wsBackendMessageLogDao(){
        return new WSBackendMessageLogDao();
    }

    @Bean
    public WSMessageLogDao wsMessageLogDao(){
        return new WSMessageLogDao();
    }

}
