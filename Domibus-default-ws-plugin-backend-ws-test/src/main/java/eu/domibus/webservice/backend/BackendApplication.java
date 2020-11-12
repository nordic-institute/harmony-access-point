package eu.domibus.webservice.backend;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.xml.ws.Endpoint;
import java.util.Collections;

/**
 * @author François Gautier
 * @since 5.0
 */
@SpringBootApplication
public class BackendApplication {

    public static final String DEFAULT_PORT = "8080";

    @Autowired
    private Bus bus;

    /**
     * Run BackendApplication with a given port number (default : {@value DEFAULT_PORT}
     * @param args 0: port number
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        String arg = args[0];
        if(StringUtils.isBlank(arg)){
            arg = DEFAULT_PORT;
        }
        app.setDefaultProperties(Collections.singletonMap("server.port", arg));
        app.run(args);
    }

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        return new ServletRegistrationBean<>(new CXFServlet(), "/*");
    }

    @Bean
    public BackendWebservice createWSPlugin() {
        return new BackendWebservice();
    }

    @Bean
    public Endpoint backendInterfaceEndpoint(BackendWebservice backendWebService) {
        return Endpoint.publish("/backend", backendWebService);
    }
}
