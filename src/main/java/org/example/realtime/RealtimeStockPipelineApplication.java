package org.example.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealtimeStockPipelineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealtimeStockPipelineApplication.class, args);
    }
}
