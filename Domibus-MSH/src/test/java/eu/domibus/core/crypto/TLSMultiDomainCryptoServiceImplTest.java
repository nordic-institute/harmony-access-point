package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.crypto.api.DomainCryptoService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;

@RunWith(JMockit.class)
public class TLSMultiDomainCryptoServiceImplTest {

    @Tested
    TLSMultiDomainCryptoServiceImpl multiDomainCryptoService;

    @Injectable
    protected ObjectProvider<TLSDomainCryptoServiceImpl> objectProvider;

    @Test
    public void createForDomain(@Mocked Domain domain, @Mocked TLSDomainCryptoServiceImpl domainCryptoService) {
        new Expectations() {{
            objectProvider.getObject(domain);
            result = domainCryptoService;
        }};
        DomainCryptoService res = multiDomainCryptoService.createForDomain(domain);

        Assert.assertEquals(domainCryptoService, res);
    }
}