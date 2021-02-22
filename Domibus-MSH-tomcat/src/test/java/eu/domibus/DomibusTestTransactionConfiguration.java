package eu.domibus;

import java.util.Properties;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.tomcat.transaction.TomcatTransactionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Ioana Dragusanu
 * @since 4.2
 */
@Configuration
public class DomibusTestTransactionConfiguration extends TomcatTransactionConfiguration {

}
