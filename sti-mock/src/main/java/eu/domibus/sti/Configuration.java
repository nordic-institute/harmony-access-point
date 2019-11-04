package eu.domibus.sti;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@org.springframework.context.annotation.Configuration
@EnableScheduling
public class Configuration {

    @Bean
    public STIAs4MessConsumer stiAs4MessConsumer(){
        return new STIAs4MessConsumer();
    }


}
