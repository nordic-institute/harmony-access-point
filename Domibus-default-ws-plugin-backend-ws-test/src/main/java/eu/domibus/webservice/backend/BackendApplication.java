package eu.domibus.webservice.backend;

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

    public static void main(int port, String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "" + port));
        app.run(args);
    }

    @Autowired
    private Bus bus;

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
