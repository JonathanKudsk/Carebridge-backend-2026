package com.carebridge.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // Don't run automatically in tests
public class PopulatorRunner implements CommandLineRunner {

    private final Populator populator;

    public PopulatorRunner(Populator populator) {
        this.populator = populator;
    }

    @Override
    public void run(String... args) throws Exception {
        populator.populate();
    }
}
