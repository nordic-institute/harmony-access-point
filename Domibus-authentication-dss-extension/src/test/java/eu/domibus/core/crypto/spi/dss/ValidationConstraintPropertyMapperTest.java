package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;
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
            @Mocked DomibusPropertyExtService domibusPropertyExtService) {
        String constraint1 = "constraint1";
        String constraint2 = "constraint2";
        List<String> constraintSuffixes=new ArrayList<>(Arrays.asList(constraint1, constraint2,"constraint3"));
        List<String> nestedProperties=new ArrayList<>(Arrays.asList("name","status"));
        new Expectations() {{
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX);
            result=constraintSuffixes;
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX +"."+constraint1);
            this.result = nestedProperties;
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX +"."+constraint2);
            this.result = nestedProperties;
            domibusPropertyExtService.getProperty(DSS_CONSTRAINTS_CONSTRAINT1_NAME);
            this.result = ADEST_IRTPTBST.name();;
            domibusPropertyExtService.getProperty( DSS_CONSTRAINTS_CONSTRAINT1_STATUS);
            this.result = "OK";
            domibusPropertyExtService.getProperty( DSS_CONSTRAINTS_CONSTRAINT2_NAME);
            this.result = QUAL_FOR_SIGN_AT_CC.name();
            domibusPropertyExtService.getProperty( DSS_CONSTRAINTS_CONSTRAINT2_STATUS);
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