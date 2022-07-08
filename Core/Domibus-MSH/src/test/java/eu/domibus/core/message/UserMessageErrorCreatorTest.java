package eu.domibus.core.message;

import eu.domibus.core.ebms3.EbMS3Exception;
import junit.framework.TestCase;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import static eu.domibus.common.ErrorCode.EBMS_0001;

public class UserMessageErrorCreatorTest {

    @Tested
    UserMessageErrorCreator userMessageErrorCreator;

    @Test
    public void createErrorResultTest(@Injectable EbMS3Exception ebm3Exception) {

        new Expectations() {{
            ebm3Exception.getRefToMessageId();
            result = "refToMessageId";

            ebm3Exception.getErrorCodeObject();
            result = EBMS_0001;

            ebm3Exception.getErrorDetail();
            result = "errorDetail";
        }};
        //when
        Assert.assertNotNull(userMessageErrorCreator.createErrorResult(ebm3Exception));

        new FullVerifications() {
        };
    }

}
