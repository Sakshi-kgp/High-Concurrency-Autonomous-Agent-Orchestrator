
package com.agent.orchestrator;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadConfigTests {

    @Test
    void executesTasksOnVirtualThreads() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                futures.add(executor.submit(
                    () -> Thread.currentThread().isVirtual()
                ));
            }

            for (Future<Boolean> future : futures) {
                assertTrue(future.get());
            }
        }
    }
}
