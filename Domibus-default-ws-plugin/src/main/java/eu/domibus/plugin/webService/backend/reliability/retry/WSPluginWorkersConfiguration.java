package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.ext.services.DomibusPropertyExtService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.DISPATCHER_CRON_EXPRESSION;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class WSPluginWorkersConfiguration {

    @Bean
    public JobDetailFactoryBean wsPluginBackendSendRetryWorker() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(WSPluginBackendSendRetryWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean wsPluginBackendSendRetryWorkerTrigger(DomibusPropertyExtService propertyExtService) {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(wsPluginBackendSendRetryWorker().getObject());
        trigger.setCronExpression(propertyExtService.getProperty(DISPATCHER_CRON_EXPRESSION));
        trigger.setStartDelay(20000);
        return trigger;
    }

}
