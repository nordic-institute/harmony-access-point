package eu.domibus.web.header;

import org.apache.http.HttpHeaders;
import org.springframework.security.web.header.HeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A service that writes a dummy Server {@code HTTP} header to the response in order to prevent leaking information.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public class ServerHeaderWriter implements HeaderWriter {

    /**
     * Linear white space value used to mask the read value of the server for security purposes.
     */
    private static final String LWS = " ";

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.SERVER, LWS);
    }
}
