package eu.domibus.core.generator.id;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
@Configuration
public class MessageIdGeneratorConfiguration {

    @Bean("domibusUUIDGenerator")
    public NoArgGenerator createUUIDGenerator() {
        final EthernetAddress ethernetAddress = EthernetAddress.fromInterface();
        return Generators.timeBasedGenerator(ethernetAddress);
    }
}
