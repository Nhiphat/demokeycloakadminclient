package com.example.demokeycloakadminclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Schedule {
    @Autowired
    private KeycloakService keycloakService;
    @Scheduled(cron = "0 0/1 * * * ?")
    public void setup() {
        keycloakService.createAccount("test", "test");
    }
}
