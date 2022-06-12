package eu.domibus.core.error;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Catalin Enache
 * @since 5.0
 */
@Configuration
public class ErrorLogDaoConfig {
    @Bean
    public ErrorLogDao errorLogDao() {
        return new ErrorLogDao();
    }

    @Bean
    public ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil() {
        return new ErrorLogEntryTruncateUtil();
    }
}




