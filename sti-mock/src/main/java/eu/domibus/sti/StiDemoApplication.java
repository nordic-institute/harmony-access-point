package eu.domibus.sti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
public class StiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(eu.domibus.sti.StiDemoApplication.class, args);
    }


}
