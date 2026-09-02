package com.baypal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// this one annotation turns on component scanning, auto-config and the embedded server
@SpringBootApplication
public class BayPalApplication {

    public static void main(String[] args) {
        SpringApplication.run(BayPalApplication.class, args);
    }
}
