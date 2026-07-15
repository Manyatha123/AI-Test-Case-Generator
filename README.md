# AI Test Case Generator

A Java-based CLI tool that generates comprehensive QA test cases from user stories using **Google Gemini LLM**. It produces **8 structured test cases** in Gherkin format — covering positive, negative, boundary, and edge case scenarios.

## Features

- Reads a plain-text user story from `input/user-story.txt`
- Calls Google Gemini API to generate test cases
- Outputs a valid `.feature` file with exactly 8 scenarios:
  - 3 Positive (happy path)
  - 3 Negative (error conditions)
  - 1 Boundary (edge of valid range)
  - 1 Edge case (unusual but plausible)
- Retry logic with exponential backoff for API reliability
- Customizable prompt template

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- A **Google Gemini API key** ([get one here](https://aistudio.google.com/apikey))

## Setup

1. **Clone the repository:**

git clone https://github.com/Manyatha123/AI-Test-Case-Generator.git
cd AI-Test-Case-Generator

2. **Create your .env file** (copy from the example):

cp .env.example .env

Then open .env and paste your Gemini API key.

3. **Build the project:**

mvn clean compile

## Usage

1. Write your user story in input/user-story.txt

2. Run the generator:

mvn exec:java -Dexec.mainClass="com.manyatha.aitcg.TestCaseGenerator"

3. Find the output in output/generated-tests.feature

## Example Output

Feature: User Login

  Scenario: Successful login with valid credentials
    Given the user is on the login page
    When the user enters a valid email and password
    And clicks the login button
    Then the user should be redirected to the dashboard

  Scenario: Login fails with incorrect password
    Given the user is on the login page
    When the user enters a valid email and an incorrect password
    And clicks the login button
    Then an error message "Invalid credentials" should be displayed

## Project Structure

AI-Test-Case-Generator/
├── pom.xml                        # Maven config
├── .env.example                   # API key template
├── input/
│   └── user-story.txt             # Your user story goes here
├── output/
│   └── generated-tests.feature    # Generated Gherkin test cases
└── src/main/java/com/manyatha/aitcg/
    ├── HelloGemini.java           # API connectivity test
    └── TestCaseGenerator.java     # Main generator logic

## Tech Stack

- Java 17
- Google Gemini Java SDK
- Jackson (JSON parsing)
- Maven

## Rate Limits

This project uses Google Gemini's free tier API. Each user has their own quota (15 requests/minute, 1500 requests/day). The built-in retry logic with exponential backoff handles temporary failures automatically. If you hit rate limits, just wait a minute and rerun.
