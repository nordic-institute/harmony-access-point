package eu.domibus.core.multitenancy;

import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class UserDomainServiceSingleDomainImplTest {

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