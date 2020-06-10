package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.UserDomain;
import eu.domibus.api.user.User;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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

//    @Test
//    public void getSuperUsers() {
//        List<User> users = userDomainServiceSingleDomainImpl.getSuperUsers();
//        Assert.assertEquals(0, users.size());
//    }

}