package com.example.app;

import com.example.lib.GreetingService;
import jakarta.inject.Inject;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@TopCommand
@Command(name = "app",
        mixinStandardHelpOptions = true,
        description = "Simple Quarkus CLI that uses a CDI bean from the lib module.")
public class AppCommand implements Runnable {

    @Option(names = {"-n", "--name"}, description = "Name to greet")
    String name;

    @Inject
    GreetingService greetingService;

    @Override
    public void run() {
        String result = greetingService.greeting(name);
        System.out.println(result);
    }
}

