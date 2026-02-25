package com.example.lib;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GreetingService {

    public String greeting(String name) {
        if (name == null || name.isBlank()) {
            return "Hello from lib!";
        }
        return "Hello, " + name + " from lib!";
    }
}

