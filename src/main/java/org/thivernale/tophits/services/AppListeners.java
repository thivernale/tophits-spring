package org.thivernale.tophits.services;

import jakarta.servlet.http.HttpSessionEvent;
import lombok.extern.slf4j.Slf4j;
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
        log.warn("Spring Application Started");
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(HttpSessionEvent event) throws InterruptedException {
        log.info("Sleeping ...");
        Thread.sleep(Duration.ofSeconds(3));
        log.warn("Session ID after sleeping {}", event.getSession()
            .getId());
    }


}
