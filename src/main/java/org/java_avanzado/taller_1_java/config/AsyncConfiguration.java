package org.java_avanzado.taller_1_java.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configures asynchronous task execution for the application.
 */
@Configuration
@Slf4j
@EnableAsync
public class AsyncConfiguration {
    /**
     * Creates the shared asynchronous {@link Executor} used by Spring {@code @Async} methods.
     *
     * @return a configured thread-pool-based task executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(corePoolSize * 10);
        executor.setThreadNamePrefix("Tasks-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
