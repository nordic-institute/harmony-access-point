package eu.domibus.core.converter;

import eu.domibus.api.party.Party;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.party.PartyResponseRo;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import mockit.Injectable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author Ioana Dragusanu
 * @since 4.1
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DomainCoreDefaultConverterTest {

    @Configuration
    @ComponentScan(basePackageClasses = {EventMapper.class, DomibusCoreMapper.class, DomainCoreDefaultConverter.class})
    @ImportResource({
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {

    }

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Injectable
    EventMapper eventMapper;

    @Autowired
    ObjectService objectService;

    @Test
    public void testConvertPartyResponseRo() throws Exception {
        PartyResponseRo toConvert = (PartyResponseRo) objectService.createInstance(PartyResponseRo.class);
        final Party converted = domainCoreConverter.convert(toConvert, Party.class);
        final PartyResponseRo convertedBack = domainCoreConverter.convert(converted, PartyResponseRo.class);
        // these fields are missing in Party, fill them so the assertion works
        convertedBack.setJoinedIdentifiers(toConvert.getJoinedIdentifiers());
        convertedBack.setJoinedProcesses(toConvert.getJoinedProcesses());
        convertedBack.setCertificateContent(toConvert.getCertificateContent());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertParty() throws Exception {
        Party toConvert = (Party) objectService.createInstance(Party.class);
        final PartyResponseRo converted = domainCoreConverter.convert(toConvert, PartyResponseRo.class);
        final Party convertedBack = domainCoreConverter.convert(converted, Party.class);
        objectService.assertObjects(convertedBack, toConvert);
    }
}
