package com.example.demo.service;

import com.example.demo.model.DocumentRaw;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FetchService Tests")
class FetchServiceTest {

    @Mock
    private MongoDocumentRepository mongoRepository;

    @InjectMocks
    private FetchService fetchService;

    @Captor
    private ArgumentCaptor<List<DocumentRaw>> documentListCaptor;

    @TempDir
    Path tempDir;

    // ==================== Tests for importDocumentsFromPipeline ====================

    @Test
    @DisplayName("importDocumentsFromPipeline - Successfully Imports Valid JSONL Files")
    void testImportDocumentsFromPipeline_Success() throws Exception {
        // Arrange - Create mock JSONL files
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\"}",
                "{\"id\":\"art-002\",\"domain\":\"blog.com\",\"title\":\"Article 2\",\"text\":\"Content 2\",\"date\":\"2024-01-16\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl",
                "{\"id\":\"speech-001\",\"domain\":\"clarin.si\",\"title\":\"Speech 1\",\"text\":\"Speech content\",\"date\":\"2024-01-15\",\"speaker_name\":\"John Doe\"}"
        );

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).deleteAll();
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());

        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(3, savedDocuments.size(), "Should save 3 documents total");

        // Verify first article
        DocumentRaw doc1 = savedDocuments.get(0);
        assertEquals("art-001", doc1.getId());
        assertEquals("news.com", doc1.getDomain());
        assertEquals("Article 1", doc1.getTitle());
        assertEquals("Content 1", doc1.getContent());
        assertEquals("2024-01-15", doc1.getDate());

        // Verify second article
        DocumentRaw doc2 = savedDocuments.get(1);
        assertEquals("art-002", doc2.getId());
        assertEquals("blog.com", doc2.getDomain());

        // Verify parlamint document
        DocumentRaw doc3 = savedDocuments.get(2);
        assertEquals("speech-001", doc3.getId());
        assertEquals("clarin.si", doc3.getDomain());
        assertEquals("John Doe", doc3.getSpeaker_name());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Missing Files Gracefully")
    void testImportDocumentsFromPipeline_MissingFiles() throws Exception {
        // Arrange - Only create one file
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\"}"
        );
        // enhanced_parlamint.jsonl does not exist

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).deleteAll();
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());

        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(1, savedDocuments.size(), "Should only save documents from existing file");
        assertEquals("art-001", savedDocuments.get(0).getId());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Skips Empty Lines")
    void testImportDocumentsFromPipeline_SkipsEmptyLines() throws Exception {
        // Arrange - Create file with empty lines
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\"}",
                "",  // Empty line
                "   ",  // Whitespace only
                "{\"id\":\"art-002\",\"domain\":\"blog.com\",\"title\":\"Article 2\",\"text\":\"Content 2\",\"date\":\"2024-01-16\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(2, savedDocuments.size(), "Should skip empty lines");
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Malformed JSON Gracefully")
    void testImportDocumentsFromPipeline_MalformedJson() throws Exception {
        // Arrange - Create file with valid and invalid JSON
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\"}",
                "{invalid json}",  // Malformed JSON
                "{\"id\":\"art-002\",\"domain\":\"blog.com\",\"title\":\"Article 2\",\"text\":\"Content 2\",\"date\":\"2024-01-16\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(2, savedDocuments.size(), "Should skip malformed JSON and continue");
        assertEquals("art-001", savedDocuments.get(0).getId());
        assertEquals("art-002", savedDocuments.get(1).getId());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Unknown JSON Fields")
    void testImportDocumentsFromPipeline_UnknownFields() throws Exception {
        // Arrange - Create JSON with extra fields not in DocumentRaw model
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\",\"unknown_field\":\"value\",\"another_unknown\":123}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(1, savedDocuments.size());

        DocumentRaw doc = savedDocuments.get(0);
        assertEquals("art-001", doc.getId());
        assertEquals("news.com", doc.getDomain());
        // Unknown fields should be ignored due to FAIL_ON_UNKNOWN_PROPERTIES = false
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Maps Text Field Correctly with @JsonAlias")
    void testImportDocumentsFromPipeline_JsonAliasMapping() throws Exception {
        // Arrange - Create JSON with "text" field (should map to "content" in DocumentRaw)
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"This is the content\",\"date\":\"2024-01-15\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();

        DocumentRaw doc = savedDocuments.get(0);
        assertEquals("This is the content", doc.getContent(),
                "@JsonAlias should map 'text' field to 'content' property");
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Documents with Topic Assignments")
    void testImportDocumentsFromPipeline_WithTopicAssignments() throws Exception {
        // Arrange - Create JSON with topicAssignment map
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content\",\"date\":\"2024-01-15\",\"topicAssignment\":{\"0\":0.35,\"2\":0.45,\"5\":0.20}}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();

        DocumentRaw doc = savedDocuments.get(0);
        assertNotNull(doc.getTopicAssignment());
        assertEquals(3, doc.getTopicAssignment().size());
        assertEquals(0.35, doc.getTopicAssignment().get(0), 0.001);
        assertEquals(0.45, doc.getTopicAssignment().get(2), 0.001);
        assertEquals(0.20, doc.getTopicAssignment().get(5), 0.001);
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles ParlaMint Metadata Fields")
    void testImportDocumentsFromPipeline_ParlaMintMetadata() throws Exception {
        // Arrange
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl", "");

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl",
                "{\"id\":\"speech-001\",\"domain\":\"clarin.si\",\"title\":\"Speech\",\"text\":\"Content\",\"date\":\"2024-01-15\"," +
                        "\"speaker_name\":\"John Doe\",\"speaker_party\":\"Labour\",\"speaker_party_name\":\"Labour Party\"," +
                        "\"speaker_gender\":\"M\",\"speaker_role\":\"MP\",\"speaker_id\":\"john-001\"," +
                        "\"speaker_mp\":\"true\",\"speaker_birth\":\"1970\",\"speaker_minister\":\"false\"," +
                        "\"party_status\":\"Coalition\",\"party_orientation\":\"Centre-left\"," +
                        "\"subcorpus\":\"Reference\",\"session\":\"Session-01\",\"body\":\"Commons\"," +
                        "\"agenda\":\"item-01\",\"topic\":\"Economics\",\"meeting\":\"meeting-01\"," +
                        "\"sitting\":\"sitting-01\",\"term\":\"2019-2024\",\"lang\":\"en\"}"
        );

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();

        DocumentRaw doc = savedDocuments.get(0);
        assertEquals("speech-001", doc.getId());
        assertEquals("John Doe", doc.getSpeaker_name());
        assertEquals("Labour", doc.getSpeaker_party());
        assertEquals("Labour Party", doc.getSpeaker_party_name());
        assertEquals("M", doc.getSpeaker_gender());
        assertEquals("MP", doc.getSpeaker_role());
        assertEquals("john-001", doc.getSpeaker_id());
        assertEquals("true", doc.getSpeaker_mp());
        assertEquals("1970", doc.getSpeaker_birth());
        assertEquals("false", doc.getSpeaker_minister());
        assertEquals("Coalition", doc.getParty_status());
        assertEquals("Centre-left", doc.getParty_orientation());
        assertEquals("Reference", doc.getSubcorpus());
        assertEquals("Session-01", doc.getSession());
        assertEquals("Commons", doc.getBody());
        assertEquals("item-01", doc.getAgenda());
        assertEquals("Economics", doc.getTopic());
        assertEquals("meeting-01", doc.getMeeting());
        assertEquals("sitting-01", doc.getSitting());
        assertEquals("2019-2024", doc.getTerm());
        assertEquals("en", doc.getLang());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Clears Existing Data Before Import")
    void testImportDocumentsFromPipeline_ClearsExistingData() throws Exception {
        // Arrange
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article 1\",\"text\":\"Content 1\",\"date\":\"2024-01-15\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).deleteAll();
        verify(mongoRepository, times(1)).saveAll(anyList());

        // Verify order: deleteAll should be called before saveAll
        var inOrder = inOrder(mongoRepository);
        inOrder.verify(mongoRepository).deleteAll();
        inOrder.verify(mongoRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Does Not Save When No Documents Found")
    void testImportDocumentsFromPipeline_NoDocuments() throws Exception {
        // Arrange - Create empty files
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl", "");
        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, never()).deleteAll();
        verify(mongoRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Large Number of Documents")
    void testImportDocumentsFromPipeline_LargeDataset() throws Exception {
        // Arrange - Create file with 1000 documents
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 1000; i++) {
            content.append(String.format(
                    "{\"id\":\"art-%04d\",\"domain\":\"news.com\",\"title\":\"Article %d\",\"text\":\"Content %d\",\"date\":\"2024-01-15\"}\n",
                    i, i, i
            ));
        }

        createMockJsonlFile("dataset/output/enhanced_articles.jsonl", content.toString().split("\n"));
        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();
        assertEquals(1000, savedDocuments.size(), "Should handle 1000 documents");

        // Verify first and last documents
        assertEquals("art-0001", savedDocuments.get(0).getId());
        assertEquals("art-1000", savedDocuments.get(999).getId());
    }

    @Test
    @DisplayName("importDocumentsFromPipeline - Handles Special Characters in Content")
    void testImportDocumentsFromPipeline_SpecialCharacters() throws Exception {
        // Arrange - JSON with special characters
        createMockJsonlFile("dataset/output/enhanced_articles.jsonl",
                "{\"id\":\"art-001\",\"domain\":\"news.com\",\"title\":\"Article with \\\"quotes\\\"\",\"text\":\"Content with\\nnewlines\\tand\\ttabs\",\"date\":\"2024-01-15\"}"
        );

        createMockJsonlFile("dataset/output/enhanced_parlamint.jsonl", "");

        // Act
        fetchService.importDocumentsFromPipeline();

        // Assert
        verify(mongoRepository, times(1)).saveAll(documentListCaptor.capture());
        List<DocumentRaw> savedDocuments = documentListCaptor.getValue();

        DocumentRaw doc = savedDocuments.get(0);
        assertTrue(doc.getTitle().contains("quotes"));
        assertTrue(doc.getContent().contains("Content"));
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to create mock JSONL files in the expected location
     */
    private void createMockJsonlFile(String relativePath, String... lines) throws IOException {
        Path filePath = Path.of(relativePath);
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (String line : lines) {
                if (line != null && !line.isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }
}