package ru.priyut.coordination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PriyutApplication {

    static void main(String[] args) {
        SpringApplication.run(PriyutApplication.class, args);
    }

}
