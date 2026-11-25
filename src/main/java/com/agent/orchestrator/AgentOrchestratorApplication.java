
package com.agent.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentOrchestratorApplication {

    public static void main(String[] args) {

        Thread browserThread = new Thread(() -> {
            try {
                Thread.sleep(5000);

                new ProcessBuilder(
                    "cmd", "/c", "start", "",
                    "http://localhost:8080"
                ).start();

            } catch (Exception e) {
                System.out.println(
                    "Could not open browser: " + e.getMessage()
                );
            }
        });

        browserThread.setDaemon(true);
        browserThread.start();

        SpringApplication.run(
            AgentOrchestratorApplication.class, args
        );
    }
}
