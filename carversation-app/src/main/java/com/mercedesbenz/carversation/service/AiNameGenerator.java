package com.mercedesbenz.carversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    static String GetUniqueRandomName(){
        AiNameGenerator aiNameGenerator = new AiNameGenerator();
        List<String> names = aiNameGenerator.getUniqueRandomNames(1);
        return names.isEmpty() ? "DefaultName" : names.get(0);
    }

    public List<String> getUniqueNamesFromAI() {
        String url = "https://genai-nexus.api.corpinter.net/apikey/openai/deployments/gpt-4o/chat/completions?api-version=2024-06-01";
        String apiKey = "";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<String> entity = getStringHttpEntity(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return parseTheNames(response);
    }

    private static HttpEntity<String> getStringHttpEntity(HttpHeaders headers) {
        String requestBody = String.format("{\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", "Provide a list of 10 unique, single-word names for a racer. Return the names as a comma-separated list only, with no additional text or formatting. Example: Name1, Name2, Name3, Name4, Name5, Name6, Name7, Name8, Name9, Name10");

        return new HttpEntity<>(requestBody, headers);
    }

    private List<String> parseTheNames(ResponseEntity<String> response) {
        try {

            // Parse the JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        String contentText = content.asText();
                        // Split the content by commas and trim whitespace
                        List<String> names = Arrays.stream(contentText.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        return names;
                    }
                }
            }
            return List.of();
        } catch (Exception e) {
            return getUniqueRandomNames(10);
        }
    }
}