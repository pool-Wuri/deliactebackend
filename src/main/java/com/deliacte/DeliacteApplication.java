package com.deliacte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeliacteApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliacteApplication.class, args);
    }
}
