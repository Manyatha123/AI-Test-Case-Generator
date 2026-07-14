package com.manyatha.aitcg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.genai.Client;
import com.google.genai.errors.ServerException;
import com.google.genai.types.GenerateContentResponse;

public class TestCaseGenerator {

    private static final Path INPUT_FILE  = Paths.get("input", "user-story.txt");
    private static final Path OUTPUT_FILE = Paths.get("output", "generated-tests.feature");
    private static final String MODEL     = "gemini-flash-latest";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 2000;

    private static final String PROMPT_TEMPLATE = """
            You are a senior QA engineer with 10+ years of experience writing
            comprehensive test cases from user stories.

            Given the user story below, generate EXACTLY 8 test cases in Gherkin
            format (.feature file syntax), following this strict distribution:

            - 3 Positive scenarios (happy path variations)
            - 3 Negative scenarios (invalid input, error conditions)
            - 1 Boundary scenario (edge of valid input range)
            - 1 Edge case scenario (unusual but plausible scenarios)

            Output rules:
            1. Return ONLY valid Gherkin content — no markdown code blocks, no
               explanations, no preamble, no closing remarks.
            2. Start with "Feature:" line describing the functionality.
            3. Each scenario should have a "Scenario:" line followed by
               Given/When/Then steps.
            4. Prefix each scenario name with its category in square brackets,
               e.g., "[POSITIVE]", "[NEGATIVE]", "[BOUNDARY]", "[EDGE]".
            5. Keep steps concrete and testable — avoid vague words like
               "properly" or "correctly".
            6. Cover ALL acceptance criteria mentioned in the user story.

            USER STORY:
            ---
            {USER_STORY_HERE}
            ---

            Generate the Gherkin feature file now:
            """;

    public static void main(String[] args) throws IOException {
        System.out.println("=== AI Test Case Generator ===");

        // 1. Read user story from disk
        String userStory = readUserStory();
        System.out.println("Read user story (" + userStory.length() + " chars) from " + INPUT_FILE);

        // 2. Inject the story into the prompt template
        String prompt = PROMPT_TEMPLATE.replace("{USER_STORY_HERE}", userStory);

        // 3. Call Gemini
        System.out.println("Calling Gemini (" + MODEL + ")... this may take a few seconds.");
        long start = System.currentTimeMillis();
        String gherkin = callGemini(prompt);
        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("Received response in " + seconds + "s (" + gherkin.length() + " chars)");

        // 4. Write to output file
        writeOutput(gherkin);
        System.out.println("Wrote " + OUTPUT_FILE.toAbsolutePath());

        // 5. Quick verification: how many scenarios did we actually get?
        long scenarioCount = gherkin.lines()
                .filter(line -> line.trim().startsWith("Scenario:"))
                .count();
        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("Scenarios generated: " + scenarioCount + " (expected 8)");
    }

    private static String readUserStory() throws IOException {
        if (!Files.exists(INPUT_FILE)) {
            throw new IOException("Input file not found: " + INPUT_FILE.toAbsolutePath()
                    + " — make sure input/user-story.txt exists in the project root.");
        }
        String content = Files.readString(INPUT_FILE).trim();
        if (content.isEmpty()) {
            throw new IOException("Input file is empty: " + INPUT_FILE.toAbsolutePath());
        }
        return content;
    }

    private static String loadApiKey() throws IOException {
        Path envFile = Paths.get(".env");
        if (!Files.exists(envFile)) {
            throw new IOException(".env file not found at " + envFile.toAbsolutePath()
                    + " — create one with the line: GEMINI_API_KEY=your_key_here");
        }
        Map<String, String> env = Files.readAllLines(envFile).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));

        String key = env.get("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IOException("GEMINI_API_KEY not found in .env file. "
                    + "Make sure the file has a line like: GEMINI_API_KEY=your_key_here");
        }
        return key;
    }

    private static String callGemini(String prompt) throws IOException {
        Client client = Client.builder().apiKey(loadApiKey()).build();

        ServerException lastError = null;
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("  Attempt " + attempt + " of " + MAX_RETRIES + "...");
                GenerateContentResponse response =
                        client.models.generateContent(MODEL, prompt, null);
                return response.text();
            } catch (ServerException e) {
                lastError = e;
                System.out.println("  Attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < MAX_RETRIES) {
                    System.out.println("  Waiting " + (backoffMs / 1000) + "s before retry...");
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                    backoffMs *= 2;
                }
            }
        }

        throw new IOException("Gemini API call failed after " + MAX_RETRIES
                + " attempts. Last error: " + lastError.getMessage(), lastError);
    }

    private static void writeOutput(String content) throws IOException {
        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(OUTPUT_FILE, content);
    }
}
