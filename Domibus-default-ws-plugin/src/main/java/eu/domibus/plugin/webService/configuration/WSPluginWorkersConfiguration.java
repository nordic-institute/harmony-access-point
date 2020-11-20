package eu.domibus.plugin.webService.configuration;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.webService.backend.reliability.retry.WSPluginBackendSendRetryWorker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.DISPATCHER_REPEAT_INTERVAL;

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
    public SimpleTriggerFactoryBean wsPluginBackendSendRetryWorkerTrigger(DomibusPropertyExtService propertyExtService) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(wsPluginBackendSendRetryWorker().getObject());
        trigger.setRepeatInterval(propertyExtService.getIntegerProperty(DISPATCHER_REPEAT_INTERVAL));
        trigger.setStartDelay(20000);
        return trigger;
    }

}
