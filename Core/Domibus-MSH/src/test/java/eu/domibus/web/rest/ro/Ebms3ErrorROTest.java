package eu.domibus.web.rest.ro;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Ebms3ErrorROTest {


    private ErrorRO errorRO;

    @Test
    public void testErrorROSerialization() {

        final String message = "message";
        final String expectedMessage = "{\"message\":\"" + message + "\"}";

        errorRO = new ErrorRO(message);

        assertEquals(expectedMessage.length(), errorRO.getContentLength());
    }

}
