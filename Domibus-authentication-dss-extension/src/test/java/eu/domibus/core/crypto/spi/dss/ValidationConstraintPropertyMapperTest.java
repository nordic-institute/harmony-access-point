package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.europa.esig.dss.validation.process.MessageTag.ADEST_IRTPTBST;
import static eu.europa.esig.dss.validation.process.MessageTag.QUAL_FOR_SIGN_AT_CC;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class ValidationConstraintPropertyMapperTest {

    @Test
    public void map(
            @Mocked DomibusPropertyExtService domibusPropertyExtService,
            @Mocked DomainContextExtService domainContextExtService,
            @Mocked Environment environment) {
        List<String> nestedProperties=new ArrayList<>(Arrays.asList("name","status"));
        new Expectations() {{
            domibusPropertyExtService.containsPropertyKey( "domibus.authentication.dss.constraint1");
            this.result = true;
            domibusPropertyExtService.containsPropertyKey( "domibus.authentication.dss.constraint2");
            this.result = true;
            domibusPropertyExtService.containsPropertyKey( "domibus.authentication.dss.constraint3");
            this.result = false;
            domibusPropertyExtService.getNestedProperties("domibus.authentication.dss.constraint1");
            this.result = nestedProperties;
            domibusPropertyExtService.getNestedProperties("domibus.authentication.dss.constraint2");
            this.result = nestedProperties;
            domibusPropertyExtService.getProperty("domibus.authentication.dss.constraint1.name");
            this.result = ADEST_IRTPTBST.name();;
            domibusPropertyExtService.getProperty( "domibus.authentication.dss.constraint1.status");
            this.result = "OK";
            domibusPropertyExtService.getProperty( "domibus.authentication.dss.constraint2.name");
            this.result = QUAL_FOR_SIGN_AT_CC.name();
            domibusPropertyExtService.getProperty("domibus.authentication.dss.constraint2.status");
            this.result = "WARNING";
        }};
        ValidationConstraintPropertyMapper constraintPropertyMapper =
                new ValidationConstraintPropertyMapper(
                        domibusPropertyExtService);

        final List<ConstraintInternal> constraints = constraintPropertyMapper.map();
        Assert.assertEquals(2, constraints.size());
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(ADEST_IRTPTBST.name()) && constraintInternal.getStatus().equals("OK")));
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(QUAL_FOR_SIGN_AT_CC.name()) && constraintInternal.getStatus().equals("WARNING")));

    }


}