package eu.domibus.tomcat;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_OUTPUT_DIR;

/**
 * This class executes before the beans from the Application Context are initialized
 */
@Component
public class TomcatApplicationPreInitializer implements BeanFactoryPostProcessor, PriorityOrdered, EnvironmentAware {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatApplicationPreInitializer.class);

    protected static final String OUTPUT_DIR = COM_ATOMIKOS_ICATCH_OUTPUT_DIR;

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
        createAtomikosOutputDirectory();
    }

    protected void createAtomikosOutputDirectory() {
        final String outputDirectory = environment.getProperty(OUTPUT_DIR);
        LOGGER.debug("Creating directory [{}]", outputDirectory);

        if (StringUtils.isEmpty(outputDirectory)) {
            LOGGER.warn("The property [{}] is not defined", OUTPUT_DIR);
            return;
        }
        try {
            FileUtils.forceMkdir(new File(outputDirectory));
        } catch (IOException e) {
            LOGGER.error("Could not create directory [{}]", outputDirectory, e);
        }
    }
}  