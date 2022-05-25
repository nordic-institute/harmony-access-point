package eu.domibus.core.user.ui.job;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.user.plugin.job.PluginUserPasswordPolicyAlertJob;
import eu.domibus.core.user.multitenancy.SuperUserPasswordPolicyAlertJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICIES_CHECK_CRON;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Configuration
public class JobConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JobConfiguration.class);
    private static final String GROUP_GENERAL = "GENERAL";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean userPasswordPolicyAlertTrigger() {

        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();

        bean.setJobDetail(userPasswordPolicyAlertJob().getObject());
        bean.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON));

        return bean;
    }

    @Bean
    public JobDetailFactoryBean userPasswordPolicyAlertJob() {

        JobDetailFactoryBean bean = new JobDetailFactoryBean();

        bean.setJobClass(UserPasswordPolicyAlertJob.class);
        bean.setDurability(true);

        return bean;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean superUserPasswordPolicyAlertTrigger() {

        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();

        bean.setJobDetail(superUserPasswordPolicyAlertJob().getObject());
        bean.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICIES_CHECK_CRON));

        bean.setGroup(GROUP_GENERAL);
        return bean;
    }

    @Bean
    public JobDetailFactoryBean superUserPasswordPolicyAlertJob() {

        JobDetailFactoryBean bean = new JobDetailFactoryBean();

        bean.setJobClass(SuperUserPasswordPolicyAlertJob.class);
        bean.setDurability(true);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean pluginUserPasswordPolicyAlertTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }
        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();

        bean.setJobDetail(pluginUserPasswordPolicyAlertJob().getObject());
        bean.setCronExpression(domibusPropertyProvider.getProperty(DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON));

        return bean;
    }

    @Bean
    public JobDetailFactoryBean pluginUserPasswordPolicyAlertJob() {

        JobDetailFactoryBean bean = new JobDetailFactoryBean();

        bean.setJobClass(PluginUserPasswordPolicyAlertJob.class);
        bean.setDurability(true);

        return bean;
    }
}