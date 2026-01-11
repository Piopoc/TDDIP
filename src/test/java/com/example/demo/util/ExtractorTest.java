package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Extractor Utility Tests")
class ExtractorTest {

    @TempDir
    Path tempDir;

    private static final ObjectMapper mapper = new ObjectMapper();

    // ==================== Tests for extractDocumentsFromTSVFile ====================

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Basic Extraction")
    void testExtractDocumentsFromTSVFile_BasicExtraction() throws IOException {
        // Arrange - Create mock TXT file with utterances
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tThis is the first speech content\n");
            writer.write("speech-002\tThis is the second speech content\n");
        }

        // Arrange - Create mock TSV metadata file
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\tSpeaker_name\n");
            writer.write("speech-001\tFirst Speech\t2024-01-15\tJohn Doe\n");
            writer.write("speech-002\tSecond Speech\t2024-01-16\tJane Smith\n");
        }

        // Arrange - Create output file
        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(2, lines.size(), "Should have 2 JSON documents");

        // Verify first document
        JsonNode doc1 = mapper.readTree(lines.get(0));
        assertEquals("speech-001", doc1.get("id").asText());
        assertEquals("clarin.si", doc1.get("domain").asText());
        assertEquals("First Speech", doc1.get("title").asText());
        assertEquals("This is the first speech content", doc1.get("text").asText());
        assertEquals("2024-01-15", doc1.get("date").asText());
        assertEquals("John Doe", doc1.get("speaker_name").asText());

        // Verify second document
        JsonNode doc2 = mapper.readTree(lines.get(1));
        assertEquals("speech-002", doc2.get("id").asText());
        assertEquals("Second Speech", doc2.get("title").asText());
        assertEquals("This is the second speech content", doc2.get("text").asText());
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Handles Missing Utterances")
    void testExtractDocumentsFromTSVFile_MissingUtterances() throws IOException {
        // Arrange - Create TXT file with only one speech
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tThis is the first speech content\n");
        }

        // Arrange - Create TSV with two speeches
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            writer.write("speech-001\tFirst Speech\t2024-01-15\n");
            writer.write("speech-999\tMissing Speech\t2024-01-16\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(2, lines.size(), "Should still output both documents");

        JsonNode doc1 = mapper.readTree(lines.get(0));
        assertEquals("This is the first speech content", doc1.get("text").asText());

        JsonNode doc2 = mapper.readTree(lines.get(1));
        assertTrue(doc2.get("text").isNull() || doc2.get("text").asText().isEmpty(),
                "Missing utterance should result in null or empty text");
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Handles Extra Metadata Fields")
    void testExtractDocumentsFromTSVFile_ExtraMetadataFields() throws IOException {
        // Arrange - Create TXT file
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tSpeech content\n");
        }

        // Arrange - Create TSV with many metadata fields
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\tSpeaker_name\tSpeaker_party\tSpeaker_gender\n");
            writer.write("speech-001\tTitle\t2024-01-15\tJohn\tLabour\tM\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        JsonNode doc = mapper.readTree(lines.get(0));

        // Verify standard fields
        assertEquals("speech-001", doc.get("id").asText());
        assertEquals("clarin.si", doc.get("domain").asText());
        assertEquals("Title", doc.get("title").asText());
        assertEquals("2024-01-15", doc.get("date").asText());

        // Verify extra fields are included
        assertEquals("John", doc.get("speaker_name").asText());
        assertEquals("Labour", doc.get("speaker_party").asText());
        assertEquals("M", doc.get("speaker_gender").asText());
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Skips Incomplete Rows")
    void testExtractDocumentsFromTSVFile_SkipsIncompleteRows() throws IOException {
        // Arrange - Create TXT file
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tContent 1\n");
            writer.write("speech-002\tContent 2\n");
        }

        // Arrange - Create TSV with incomplete row
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            writer.write("speech-001\tTitle 1\t2024-01-15\n");
            writer.write("speech-002\tTitle 2\n");  // Missing Date column
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size(), "Should only process complete rows");

        JsonNode doc = mapper.readTree(lines.get(0));
        assertEquals("speech-001", doc.get("id").asText());
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Handles Special Characters")
    void testExtractDocumentsFromTSVFile_SpecialCharacters() throws IOException {
        // Arrange - Create TXT with special characters
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tText with \"quotes\" and \ttabs\n");
        }

        // Arrange - Create TSV
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            writer.write("speech-001\tTitle with, comma\t2024-01-15\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        JsonNode doc = mapper.readTree(lines.get(0));

        assertTrue(doc.get("text").asText().contains("quotes"));
        assertTrue(doc.get("title").asText().contains("comma"));
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Empty TXT File")
    void testExtractDocumentsFromTSVFile_EmptyTxtFile() throws IOException {
        // Arrange - Create empty TXT file
        File txtFile = tempDir.resolve("test.txt").toFile();
        txtFile.createNewFile();

        // Arrange - Create TSV
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            writer.write("speech-001\tTitle\t2024-01-15\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(1, lines.size());

        JsonNode doc = mapper.readTree(lines.get(0));
        assertTrue(doc.get("text").isNull() || doc.get("text").asText().isEmpty());
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Case Insensitive Header Mapping")
    void testExtractDocumentsFromTSVFile_CaseInsensitiveHeaders() throws IOException {
        // Arrange - Create TXT file
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tContent\n");
        }

        // Arrange - Create TSV with mixed case headers
        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTITLE\tDATE\n");
            writer.write("speech-001\tTest Title\t2024-01-15\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        JsonNode doc = mapper.readTree(lines.get(0));

        // Headers are converted to lowercase, so mapping should work
        assertEquals("Test Title", doc.get("title").asText());
        assertEquals("2024-01-15", doc.get("date").asText());
    }

    // ==================== Tests for extractAll ====================

    @Test
    @DisplayName("extractAll - Directory Not Found")
    void testExtractAll_DirectoryNotFound() {
        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            Extractor.extractAll("INVALID_COUNTRY");
        });

        assertTrue(exception.getMessage().contains("non trovato") ||
                exception.getMessage().contains("non valido"));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Handles Tab in Utterance Content")
    void testExtractDocumentsFromTSVFile_TabInContent() throws IOException {
        // Arrange - Create TXT with tabs in content (should handle split limit)
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write("speech-001\tThis is content\twith embedded\ttabs\n");
        }

        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            writer.write("speech-001\tTitle\t2024-01-15\n");
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        JsonNode doc = mapper.readTree(lines.get(0));

        // The split with limit=2 should preserve everything after first tab
        String text = doc.get("text").asText();
        assertTrue(text.contains("content"));
        assertTrue(text.contains("with"));
    }

    @Test
    @DisplayName("extractDocumentsFromTSVFile - Multiple Documents Performance")
    void testExtractDocumentsFromTSVFile_MultipleDocuments() throws IOException {
        // Arrange - Create files with 100 documents
        File txtFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            for (int i = 1; i <= 100; i++) {
                writer.write(String.format("speech-%03d\tContent for speech %d\n", i, i));
            }
        }

        File tsvFile = tempDir.resolve("test-meta.tsv").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tsvFile))) {
            writer.write("ID\tTitle\tDate\n");
            for (int i = 1; i <= 100; i++) {
                writer.write(String.format("speech-%03d\tTitle %d\t2024-01-15\n", i, i));
            }
        }

        File outputFile = tempDir.resolve("output.jsonl").toFile();
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Act
        long startTime = System.currentTimeMillis();
        Extractor.extractDocumentsFromTSVFile(tsvFile, outputWriter, "GB");
        outputWriter.close();
        long endTime = System.currentTimeMillis();

        // Assert
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(100, lines.size(), "Should process all 100 documents");

        // Verify first and last documents
        JsonNode firstDoc = mapper.readTree(lines.get(0));
        assertEquals("speech-001", firstDoc.get("id").asText());

        JsonNode lastDoc = mapper.readTree(lines.get(99));
        assertEquals("speech-100", lastDoc.get("id").asText());

        // Performance check (should be fast for 100 docs)
        assertTrue(endTime - startTime < 5000, "Should process 100 documents in under 5 seconds");
    }
}