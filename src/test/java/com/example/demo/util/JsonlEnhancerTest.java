package com.example.demo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonlEnhancer Integration Tests")
class JsonlEnhancerTest {

    // JUnit creates this temporary folder, then it destroys that
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should reorder fields and remove unwanted columns")
    void testProcessFile_ReorderingAndCleaning() throws IOException {
        Path inputFile = tempDir.resolve("input.jsonl");
        Path outputFile = tempDir.resolve("output.jsonl");

        String jsonInput = "{\"sources\": \"web\", \"documentLength\": 100, \"text\": \"Contenuto\", \"id\": \"123\", \"title\": \"Titolo\"}";
        Files.writeString(inputFile, jsonInput);

        JsonlEnhancer.processFile(inputFile.toString(), outputFile.toString());

        assertTrue(Files.exists(outputFile), "output file has to be created");
        String content = Files.readString(outputFile).trim();

        // 'id' must be first
        assertTrue(content.startsWith("{\"id\":"), "ID should be the first field");

        // 'sources' and 'documentLength' must NOT be present
        assertFalse(content.contains("\"sources\""), "'sources' field had to be removed");
        assertFalse(content.contains("\"documentLength\""), "'documentLength' field had to be removed");

        // data have to be present
        assertTrue(content.contains("\"text\":\"Contenuto\""), "The text content should match the input");
    }

    @Test
    @DisplayName("Should flatten additionalFields when ID is NOT numeric")
    void testProcessFile_FlatteningNonNumericId() throws IOException {

        Path inputFile = tempDir.resolve("parlamint_input.jsonl");
        Path outputFile = tempDir.resolve("parlamint_output.jsonl");

        String jsonInput = "{\"id\": \"parlamint-01\", \"additionalFields\": {\"speaker\": \"Mario\", \"party\": \"Red\"}}";
        Files.writeString(inputFile, jsonInput);

        JsonlEnhancer.processFile(inputFile.toString(), outputFile.toString());

        String content = Files.readString(outputFile).trim();

        assertFalse(content.contains("\"additionalFields\""), "additionalFields has to be removed");
        assertTrue(content.contains("\"speaker\":\"Mario\""), "Inside fields have to be extract");
    }


    @Test
    @DisplayName("Should extract fields from additionalFields when ID is NOT numeric")
    void testProcessFile_ExtractsFieldsForNonNumericId() throws IOException {
         Path inputFile = tempDir.resolve("news_input.jsonl");
        Path outputFile = tempDir.resolve("news_output.jsonl");

        String jsonInput = "{\"id\": \"12345\", \"additionalFields\": {\"author\": \"Luigi\"}}";
        Files.writeString(inputFile, jsonInput);

        JsonlEnhancer.processFile(inputFile.toString(), outputFile.toString());

        String content = Files.readString(outputFile).trim();

       assertFalse(content.contains("\"author\":\"Luigi\""), "AdditionalFields is ignored and not extract if the ID is composed by numbers");
    }

    @Test
    @DisplayName("Should handle file not found gracefully")
    void testProcessFile_FileNotFound() {
        // verify that will not be a crash
        assertDoesNotThrow(() ->
                JsonlEnhancer.processFile("fake/path.jsonl", "output.jsonl")
        );
    }

    @Test
    @DisplayName("Should handle empty lines in input")
    void testProcessFile_SkipEmptyLines() throws IOException {
        Path inputFile = tempDir.resolve("empty_lines.jsonl");
        Path outputFile = tempDir.resolve("output_empty.jsonl");

        // json - empty erow - json
        String content = "{\"id\":\"1\"}\n\n   \n{\"id\":\"2\"}";
        Files.writeString(inputFile, content);

        JsonlEnhancer.processFile(inputFile.toString(), outputFile.toString());

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size(), "Empty line should be skipped");
    }
}

