package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.i18n.MessageTag;
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
        MessageTag trustAnchorConstraint = MessageTag.BBB_XCV_CCCBB;
        MessageTag qualTlFresh = MessageTag.QUAL_TL_FRESH;
        List<String> constraintSuffixes = new ArrayList<>(Arrays.asList(constraint1, constraint2, "constraint3"));
        List<String> nestedProperties = new ArrayList<>(Arrays.asList("name", "status"));
        new Expectations() {{
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX);
            result = constraintSuffixes;
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX + "." + constraint1);
            this.result = nestedProperties;
            domibusPropertyExtService.getNestedProperties(CONSTRAINTS_PREFIX + "." + constraint2);
            this.result = nestedProperties;
            domibusPropertyExtService.getProperty(DSS_CONSTRAINTS_CONSTRAINT1_NAME);

            this.result = trustAnchorConstraint.name();
            domibusPropertyExtService.getProperty(DSS_CONSTRAINTS_CONSTRAINT1_STATUS);
            this.result = "OK";
            domibusPropertyExtService.getProperty(DSS_CONSTRAINTS_CONSTRAINT2_NAME);
            this.result = qualTlFresh.name();
            domibusPropertyExtService.getProperty(DSS_CONSTRAINTS_CONSTRAINT2_STATUS);
            this.result = "WARNING";
        }};
        ValidationConstraintPropertyMapper constraintPropertyMapper =
                new ValidationConstraintPropertyMapper(
                        domibusPropertyExtService);

        final List<ConstraintInternal> constraints = constraintPropertyMapper.map();
        Assert.assertEquals(2, constraints.size());
        Assert.assertTrue(constraints.stream()
                .anyMatch(constraintInternal -> constraintInternal.getName().equals(trustAnchorConstraint.name()) && constraintInternal.getStatus().equals("OK")));
        Assert.assertTrue(constraints.stream()
                .anyMatch(constraintInternal -> constraintInternal.getName().equals(qualTlFresh.name()) && constraintInternal.getStatus().equals("WARNING")));

    }


}