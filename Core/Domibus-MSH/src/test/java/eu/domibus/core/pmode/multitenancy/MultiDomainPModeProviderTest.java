package eu.domibus.core.pmode.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.ConfigurationRawDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.provider.PModeProviderFactoryImpl;
import eu.domibus.core.pmode.validation.PModeValidationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MultiDomainPModeProviderTest {

    protected volatile Map<Domain, PModeProvider> providerMap = new HashMap<>();

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PModeProviderFactoryImpl pModeProviderFactory;

    @Injectable
    protected ConfigurationDAO configurationDAO;

    @Injectable
    protected ConfigurationRawDAO configurationRawDAO;

    @Injectable
    protected EntityManager entityManager;

    @Injectable
    private JAXBContext jaxbContextConfig;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    PModeValidationService pModeValidationService;


    @Injectable
    protected SignalService signalService;

    @Injectable
    protected MpcService mpcService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    MultiDomainPModeProvider multiDomainPModeProvider;

    @Test
    public void testGetCurrentPModeProvider(@Injectable Domain currentDomain, @Injectable PModeProvider pModeProvider) {
        multiDomainPModeProvider.providerMap = providerMap;

        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = currentDomain;

            pModeProviderFactory.createDomainPModeProvider(currentDomain);
            result = pModeProvider;
        }};

        multiDomainPModeProvider.getCurrentPModeProvider();
        Assert.assertTrue(providerMap.containsKey(currentDomain));

        multiDomainPModeProvider.getCurrentPModeProvider();

        new Verifications() {{
            pModeProviderFactory.createDomainPModeProvider(currentDomain);
            times = 1;
        }};


    }
}
