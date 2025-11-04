package org.thivernale.tophits;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.thivernale.tophits.repositories.TrackRepository;

@SpringBootApplication
public class TophitsSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TophitsSpringApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(ApplicationContext ctx, TrackRepository repo) {
        return args -> {
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            java.util.Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            System.out.println(repo.count());
        };
    }
}
