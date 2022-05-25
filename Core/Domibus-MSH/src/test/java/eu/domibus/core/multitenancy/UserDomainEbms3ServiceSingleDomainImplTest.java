package eu.domibus.core.multitenancy;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class UserDomainEbms3ServiceSingleDomainImplTest {

    @Tested
    UserDomainServiceSingleDomainImpl userDomainServiceSingleDomainImpl;

    @Test
    public void getDomainForUser() {
        String domainCode = userDomainServiceSingleDomainImpl.getDomainForUser("user1");
        Assert.assertEquals("default", domainCode);
    }

    @Test
    public void getPreferredDomainForUser() {
        String domainCode = userDomainServiceSingleDomainImpl.getPreferredDomainForUser("user1");
        Assert.assertEquals("default", domainCode);
    }

}
