package org.thivernale.tophits.services;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

//@Component
public class AllApplicationEventsListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("-------------" + event.toString());
    }
}
