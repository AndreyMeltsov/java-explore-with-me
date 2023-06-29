package ru.practicum.ewmservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.statclient.StatClient;

@SpringBootApplication(scanBasePackageClasses = {EwmMainServer.class, StatClient.class})
public class EwmMainServer {

    public static void main(String[] args) {
        SpringApplication.run(EwmMainServer.class, args);
    }
}