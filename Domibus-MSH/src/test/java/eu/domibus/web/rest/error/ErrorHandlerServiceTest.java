package eu.domibus.web.rest.error;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.ro.ErrorRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.Path;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class ErrorHandlerServiceTest {

    @Tested
    ErrorHandlerService errorHandlerService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testCreateResponseWithStatus() {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "Error occurred";
        Exception ex = new Exception(errorMessage);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty("domibus.exceptions.rest.enable");
            result = true;
        }};

        ResponseEntity<ErrorRO> result = errorHandlerService.createResponse(ex, status);

        assertEquals(status, result.getStatusCode());
        assertEquals("close", result.getHeaders().get(HttpHeaders.CONNECTION).get(0));
        assertEquals(errorMessage, result.getBody().getMessage());
    }

    @Test
    public void testCreateResponse() {
        String errorMessage = "Error occurred";
        Exception ex = new Exception(errorMessage);
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty("domibus.exceptions.rest.enable");
            result = true;
        }};

        ResponseEntity<ErrorRO> result = errorHandlerService.createResponse(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("close", result.getHeaders().get(HttpHeaders.CONNECTION).get(0));
        assertEquals(errorMessage, result.getBody().getMessage());
    }

    @Test
    public void getLast(@Mocked Path propertyPath, @Mocked Iterator<Path.Node> it, @Mocked Path.Node pn1, @Mocked Path.Node pn2) {
        new Expectations() {{
            propertyPath.iterator();
            result = it;
            it.next();
            returns(pn1, pn2);
            it.hasNext();
            returns(true, true, false);
            pn2.toString();
            result = "pn2_value";
        }};
        String res = errorHandlerService.getLast(propertyPath);
        assertEquals("pn2_value", res);
    }
}
