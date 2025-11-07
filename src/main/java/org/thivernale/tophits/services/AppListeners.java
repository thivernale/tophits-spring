package org.thivernale.tophits.services;

import jakarta.servlet.http.HttpSessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
@Slf4j
public class AppListeners {

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Spring Application Started {}", event.getApplicationContext()
            .getId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(HttpSessionEvent event) throws InterruptedException {
        log.info("Sleeping ...");
        Thread.sleep(Duration.ofSeconds(0));
        log.warn("Session ID after sleeping {}", event.getSession()
            .getId());
    }

    @EventListener
    public void webServerInitialized(WebServerInitializedEvent event) {
        log.info("WebServer started on port {}", event.getWebServer()
            .getPort());
    }

    @EventListener
    public void ready(ApplicationReadyEvent event) {
        log.info("Application ready in {} seconds", event.getTimeTaken()
            .toMillis() / 1000.0);
    }

    @EventListener
    public void onCustomApplicationEvent(CustomAppEvent event) {
        log.info("Custom application event {}", event);
    }

    public record CustomAppEvent(Object source) {
    }
}
