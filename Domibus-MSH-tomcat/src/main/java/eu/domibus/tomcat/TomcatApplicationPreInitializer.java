package eu.domibus.tomcat;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * This class executes before the beans from the Application Context are initialized
 */
@Component
public class TomcatApplicationPreInitializer implements BeanFactoryPostProcessor, PriorityOrdered, EnvironmentAware {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatApplicationPreInitializer.class);

    protected Environment environment;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
