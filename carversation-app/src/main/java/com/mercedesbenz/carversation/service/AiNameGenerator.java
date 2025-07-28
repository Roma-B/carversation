package com.mercedesbenz.carversation.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AiNameGenerator {

    private static final List<String> NAMES = List.of(
            // Simple/Short Names
            "Alex", "Sam", "Max", "Kai", "Jude", "Rory", "Sky", "Ash", "Drew", "Rey",
            // Car/Speed Inspired
            "Dash", "Axel", "Jet", "Vince", "Miles", "Troy", "Zane", "Lex", "Nova", "Nico",
            // Futuristic Style
            "Orin", "Zion", "Vega", "Cade", "Kato", "Sage", "Rune", "Ezri"
    );

    public List<String> getUniqueRandomNames(int count) {
        if (count > NAMES.size()) {
            throw new IllegalArgumentException("Requested more names than available");
        }
        List<String> copy = new ArrayList<>(NAMES);
        Collections.shuffle(copy);
        return copy.subList(0, count);
    }
}