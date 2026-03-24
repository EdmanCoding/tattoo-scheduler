package com.tattoo.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TattooSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TattooSchedulerApplication.class, args);
    }

}
