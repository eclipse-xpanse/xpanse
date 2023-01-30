package org.eclipse.osc.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.eclipse.osc")
public class OscApplication {

    public static void main(String[] args) {
        SpringApplication.run(OscApplication.class, args);
    }

}