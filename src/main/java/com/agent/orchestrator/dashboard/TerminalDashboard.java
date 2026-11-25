
package com.agent.orchestrator.dashboard;

import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "dashboard.enabled",
        havingValue = "true"
)
public class TerminalDashboard implements ApplicationRunner {

    private final AgentRegistrationService registrationService;
    private final DashboardMetricsService metricsService;

    public TerminalDashboard(
            AgentRegistrationService registrationService,
            DashboardMetricsService metricsService) {
        this.registrationService = registrationService;
        this.metricsService = metricsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread.ofVirtual()
                .name("terminal-dashboard")
                .start(this::startDashboard);
    }

    private void startDashboard() {
        try {
            Screen screen = new TerminalScreen(
                    new DefaultTerminalFactory().createTerminal()
            );

            screen.startScreen();
            screen.setCursorPosition(null);

            boolean running = true;

            while (running) {
                drawDashboard(screen);

                var key = screen.pollInput();

                if (key != null
                        && key.getCharacter() != null
                        && (key.getCharacter() == 'q'
                        || key.getCharacter() == 'Q')) {
                    running = false;
                }

                Thread.sleep(1000);
            }

            screen.stopScreen();

        } catch (IOException e) {
            System.err.println(
                    "Dashboard terminal error: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void drawDashboard(Screen screen) throws IOException {

        screen.clear();

        TextGraphics graphics = screen.newTextGraphics();
        TerminalSize size = screen.getTerminalSize();

        int width = size.getColumns();
        int height = size.getRows();

        graphics.setForegroundColor(TextColor.ANSI.CYAN);
        graphics.enableModifiers(SGR.BOLD);
        graphics.putString(
                2, 0,
                "HIGH-CONCURRENCY AUTONOMOUS AGENT ORCHESTRATOR"
        );

        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.disableModifiers(SGR.BOLD);

        graphics.putString(
                2, 2,
                "Registered Agents : "
                        + registrationService.getAllAgents().size()
        );

        graphics.putString(
                2, 3,
                "Active Tasks      : "
                        + metricsService.getActiveTasks()
        );

        graphics.putString(
                2, 4,
                "Completed Tasks   : "
                        + metricsService.getCompletedTasks()
        );

        graphics.putString(
                2, 5,
                "Failed Tasks      : "
                        + metricsService.getFailedTasks()
        );

        graphics.putString(
                2, 6,
                "Average Latency   : "
                        + String.format(
                        "%.2f ms",
                        metricsService.getAverageExecutionTime()
                )
        );

        int dividerX = Math.max(35, width / 2);

        graphics.putString(
                2, 8,
                "REGISTERED AGENTS"
        );

        graphics.putString(
                dividerX + 2, 8,
                "TASK METRICS"
        );

        graphics.putString(
                2, 9,
                "-".repeat(Math.max(1, width - 4))
        );

        graphics.putString(
                dividerX, 9,
                "|"
        );

        List<RegisteredAgent> agents =
                registrationService.getAllAgents();

        int row = 10;

        for (RegisteredAgent agent : agents) {

            if (row >= height - 3) {
                break;
            }

            graphics.putString(
                    2, row,
                    agent.agentId() + " | " + agent.agentName()
            );

            row++;

            if (row >= height - 3) {
                break;
            }

            graphics.putString(
                    4, row,
                    "Type: " + agent.agentType()
            );

            row++;

            if (row >= height - 3) {
                break;
            }

            graphics.putString(
                    4, row,
                    "Address: " + agent.agentAddress()
            );

            row++;
        }

        graphics.putString(
                dividerX, 10,
                "| Active: " + metricsService.getActiveTasks()
        );

        graphics.putString(
                dividerX, 11,
                "| Completed: " + metricsService.getCompletedTasks()
        );

        graphics.putString(
                dividerX, 12,
                "| Failed: " + metricsService.getFailedTasks()
        );

        graphics.putString(
                dividerX, 13,
                "| Total: " + metricsService.getTotalTasks()
        );

        graphics.putString(
                2, height - 2,
                "Press Q to exit dashboard | Refresh interval: 1 second"
        );

        screen.refresh();
    }
}
