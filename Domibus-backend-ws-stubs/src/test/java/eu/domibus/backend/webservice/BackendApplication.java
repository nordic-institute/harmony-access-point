package eu.domibus.backend.webservice;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.xml.ws.Endpoint;

/**
 * @author François Gautier
 * @since 5.0
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Autowired
    private Bus bus;

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        return new ServletRegistrationBean<>(new CXFServlet(), "/*");
    }

    @Bean
    public BackendClient createWSPlugin() {
        return new BackendClient();
    }

    @Bean
    public Endpoint backendInterfaceEndpoint(BackendClient backendWebService    ) {
        return Endpoint.publish("/backend", backendWebService);
    }
}
