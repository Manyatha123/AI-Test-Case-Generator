package com.manyatha.aitcg;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class HelloGemini {

    private static final String MODEL = "gemini-flash-latest";

    public static void main(String[] args) {
        try {
            // 1. Load API key from .env
            String apiKey = loadApiKey();
            System.out.println("🔑 API key loaded (length: " + apiKey.length() + ")");

            // 2. Initialize the Gemini client
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            System.out.println("🚀 Gemini client initialized");

            // 3. Send a prompt
            String prompt = "Say hello in a fun, creative way in one sentence. "
                          + "Pretend you're excited to help a QA engineer named Manyatha.";

            System.out.println("\n📤 Sending prompt: " + prompt);
            System.out.println("⏳ Waiting for Gemini's response...\n");

            GenerateContentResponse response = client.models.generateContent(
                    MODEL,
                    prompt,
                    null
            );

            // 4. Print the response
            String reply = response.text();
            System.out.println("💬 Gemini says:");
            System.out.println("─────────────────────────────────");
            System.out.println(reply);
            System.out.println("─────────────────────────────────");

            System.out.println("\n✅ Success! You just made your first LLM API call in Java.");

        } catch (Exception e) {
            System.err.println("❌ Something went wrong:");
            e.printStackTrace();
        }
    }

    /**
     * Load the API key from .env file at the project root.
     */
    private static String loadApiKey() throws IOException {
        Map<String, String> env = Files.readAllLines(Paths.get(".env")).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));

        String key = env.get("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY not found in .env file");
        }
        return key;
    }
}