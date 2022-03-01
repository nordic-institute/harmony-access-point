package eu.domibus.core.converter;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * @author François Gautier
 * @since 5.0
 */
public class BackendFilterCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private BackendFilterCoreMapper backendFilterCoreMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void convertBackendFilter() {
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        final MessageFilterRO converted = backendFilterCoreMapper.backendFilterToMessageFilterRO(toConvert);
        final BackendFilter convertedBack = backendFilterCoreMapper.messageFilterROToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertBackendFilterEntity() {
        Random random = new Random();
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        toConvert.setEntityId(""+random.nextLong());
        for (RoutingCriteria routingCriteria : toConvert.getRoutingCriterias()) {
            routingCriteria.setEntityId(""+random.nextLong());
        }
        final BackendFilterEntity converted = backendFilterCoreMapper.backendFilterToBackendFilterEntity(toConvert);
        final BackendFilter convertedBack = backendFilterCoreMapper.backendFilterEntityToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

}