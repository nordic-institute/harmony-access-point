package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
@DependsOn({"springContextProvider"})
public class FSWorkersConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSWorkersConfiguration.class);

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Bean
    public JobDetailFactoryBean fsPluginSendMessagesWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSSendMessagesWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SimpleTriggerFactoryBean fsPluginSendMessagesWorkerTrigger() {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (domain == null) {
            return null; // this job only works for a domain
        }
        String domainCode = domain.getCode();
        SimpleTriggerFactoryBean obj = new SimpleTriggerFactoryBean();
        obj.setJobDetail(fsPluginSendMessagesWorkerJob().getObject());
        obj.setRepeatInterval(fsPluginProperties.getSendWorkerInterval(domainCode));
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean fsPluginPurgeSentWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSPurgeSentWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean fsPluginPurgeSentWorkerTrigger() {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (domain == null) {
            return null; // this job only works for a domain
        }
        String domainCode = domain.getCode();
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeSentWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getSentPurgeWorkerCronExpression(domainCode));
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean fsPluginPurgeFailedWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSPurgeFailedWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean fsPluginPurgeFailedWorkerTrigger() {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (domain == null) {
            return null; // this job only works for a domain
        }
        String domainCode = domain.getCode();
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeFailedWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getFailedPurgeWorkerCronExpression(domainCode));
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean fsPluginPurgeReceivedWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSPurgeReceivedWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean fsPluginPurgeReceivedWorkerTrigger() {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (domain == null) {
            return null; // this job only works for a domain
        }
        String domainCode = domain.getCode();
        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeReceivedWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getReceivedPurgeWorkerCronExpression(domainCode));
        obj.setStartDelay(20000);
        return obj;
    }
}
