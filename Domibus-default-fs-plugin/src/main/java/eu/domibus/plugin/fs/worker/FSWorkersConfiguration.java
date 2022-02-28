package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@Configuration
public class FSWorkersConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSWorkersConfiguration.class);

    protected final DomainContextExtService domainContextExtService;

    protected final DomibusSchedulerExtService domibusSchedulerExtService;

    public FSWorkersConfiguration(DomainContextExtService domainContextExtService,
                                  DomibusSchedulerExtService domibusSchedulerExtService) {
        this.domainContextExtService = domainContextExtService;
        this.domibusSchedulerExtService = domibusSchedulerExtService;
    }

    @Bean
    public JobDetailFactoryBean fsPluginSendMessagesWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSSendMessagesWorker.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SimpleTriggerFactoryBean fsPluginSendMessagesWorkerTrigger(FSPluginProperties fsPluginProperties) {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (!checkTriggerCreation(fsPluginProperties, domain, "fsPluginSendMessagesWorkerJob")) {
            return null;
        }

        SimpleTriggerFactoryBean obj = new SimpleTriggerFactoryBean();
        obj.setJobDetail(fsPluginSendMessagesWorkerJob().getObject());
        obj.setRepeatInterval(fsPluginProperties.getSendWorkerInterval(domain.getCode()));
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
    public CronTriggerFactoryBean fsPluginPurgeSentWorkerTrigger(FSPluginProperties fsPluginProperties) {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (!checkTriggerCreation(fsPluginProperties, domain, "fsPluginPurgeSentWorkerJob")) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeSentWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getSentPurgeWorkerCronExpression(domain.getCode()));
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
    public CronTriggerFactoryBean fsPluginPurgeFailedWorkerTrigger(FSPluginProperties fsPluginProperties) {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (!checkTriggerCreation(fsPluginProperties, domain, "fsPluginPurgeFailedWorkerJob")) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeFailedWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getFailedPurgeWorkerCronExpression(domain.getCode()));
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
    public CronTriggerFactoryBean fsPluginPurgeReceivedWorkerTrigger(FSPluginProperties fsPluginProperties) {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (!checkTriggerCreation(fsPluginProperties, domain, "fsPluginPurgeReceivedWorkerJob")) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeReceivedWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getReceivedPurgeWorkerCronExpression(domain.getCode()));
        obj.setStartDelay(20000);
        return obj;
    }

    @Bean
    public JobDetailFactoryBean fsPluginPurgeLocksWorkerJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(FSPurgeLocksWorker.class);
        obj.setDurability(true);
        return obj;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean fsPluginPurgeLocksWorkerTrigger(FSPluginProperties fsPluginProperties) {
        DomainDTO domain = domainContextExtService.getCurrentDomainSafely();
        if (!checkTriggerCreation(fsPluginProperties, domain, "fsPluginPurgeLocksWorkerJob")) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(fsPluginPurgeLocksWorkerJob().getObject());
        obj.setCronExpression(fsPluginProperties.getLocksPurgeWorkerCronExpression(domain.getCode()));
        obj.setStartDelay(20000);
        return obj;
    }


    protected boolean checkTriggerCreation(FSPluginProperties fsPluginProperties, DomainDTO domain, String jobName) {
        if (domain == null) {
            LOG.debug("we cannot create {}, domain is null", jobName);
            return false; // this job only works for a domain
        }
        if (!fsPluginProperties.getDomainEnabled(domain.getCode())) {
            LOG.debug("Domain [{}] is disabled, job [{}] will be created but paused", jobName, domain);

            domibusSchedulerExtService.markJobForPausing(domain.getCode(), jobName);
            LOG.debug("Quartz job [{}] marked for pausing", jobName);
        }
        return true;
    }
}
