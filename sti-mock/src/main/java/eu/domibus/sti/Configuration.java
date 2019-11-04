package eu.domibus.sti;

import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public STIAs4MessConsumer stiAs4MessConsumer(){
        return new STIAs4MessConsumer();
    }


}
