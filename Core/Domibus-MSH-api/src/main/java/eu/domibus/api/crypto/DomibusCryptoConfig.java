package eu.domibus.core.crypto;

import org.apache.wss4j.common.crypto.CryptoType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
@Configuration
public class DomibusCryptoConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public DomibusCryptoType domibusCryptoType(CryptoType cryptoType) {
        return new DomibusCryptoType(cryptoType);
    }}
