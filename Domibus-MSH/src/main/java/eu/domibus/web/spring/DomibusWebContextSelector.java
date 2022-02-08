package eu.domibus.web.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.api.spring.DomibusWebContext;
import org.reflections.Reflections;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsible for detecting Domibus Spring Web Configurations and adding them to the main Spring Web Context
 */
public class DomibusWebContextSelector implements ImportSelector {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusWebContextSelector.class);

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Reflections reflections = new Reflections("eu.domibus");
        Set<Class<? extends Object>> allClasses = reflections.getTypesAnnotatedWith(DomibusWebContext.class);
        List<String> webConfigurations = allClasses.stream().map(aClass -> aClass.getCanonicalName()).collect(Collectors.toList());
        LOG.info("Importing the following Configuration classes to the Spring web context: [{}]", webConfigurations);

        return webConfigurations.toArray(new String[0]);
    }
}