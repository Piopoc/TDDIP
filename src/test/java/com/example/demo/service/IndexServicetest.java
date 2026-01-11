package com.example.demo.service;

import com.example.demo.model.DocumentRaw;
import com.example.demo.model.DocumentSearch;
import com.example.demo.repository.elastic.ElasticDocumentRepository;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IndexService Tests")
class IndexServiceTest {

    @Mock
    private MongoDocumentRepository mongoDocumentRepository;

    @Mock
    private ElasticDocumentRepository elasticDocumentRepository;

    @InjectMocks
    private IndexService indexService;

    @Captor
    private ArgumentCaptor<List<DocumentSearch>> searchDocumentListCaptor;

    private DocumentRaw testDocumentRaw;
    private List<DocumentRaw> testDocumentsRaw;

    @BeforeEach
    void setUp() {
        // Setup test data
        testDocumentRaw = new DocumentRaw();
        testDocumentRaw.setId("doc-001");
        testDocumentRaw.setTitle("Test Document");
        testDocumentRaw.setContent("This is test content");
        testDocumentRaw.setDomain("test.com");
        testDocumentRaw.setDate("2024-01-15");

        DocumentRaw testDocumentRaw2 = new DocumentRaw();
        testDocumentRaw2.setId("doc-002");
        testDocumentRaw2.setTitle("Another Document");
        testDocumentRaw2.setContent("Another test content");
        testDocumentRaw2.setDomain("example.com");
        testDocumentRaw2.setDate("2024-01-16");

        testDocumentsRaw = Arrays.asList(testDocumentRaw, testDocumentRaw2);
    }

    // ==================== Tests for syncMongoElastic ====================

    @Test
    @DisplayName("syncMongoElastic - Successfully Syncs Documents")
    void testSyncMongoElastic_Success() {
        // Arrange
        when(mongoDocumentRepository.findAll()).thenReturn(testDocumentsRaw);

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(mongoDocumentRepository, times(1)).findAll();
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());

        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();
        assertEquals(2, savedDocs.size(), "Should sync 2 documents");

        // Verify first document mapping
        DocumentSearch doc1 = savedDocs.get(0);
        assertEquals("doc-001", doc1.getId());
        assertEquals("Test Document", doc1.getTitle());
        assertEquals("This is test content", doc1.getContent());
        assertEquals("test.com", doc1.getDomain());

        // Verify second document mapping
        DocumentSearch doc2 = savedDocs.get(1);
        assertEquals("doc-002", doc2.getId());
        assertEquals("Another Document", doc2.getTitle());
        assertEquals("Another test content", doc2.getContent());
        assertEquals("example.com", doc2.getDomain());
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Empty MongoDB Collection")
    void testSyncMongoElastic_EmptyMongo() {
        // Arrange
        when(mongoDocumentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(mongoDocumentRepository, times(1)).findAll();
        verify(elasticDocumentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("syncMongoElastic - Maps Topic Assignments Correctly")
    void testSyncMongoElastic_WithTopicAssignments() {
        // Arrange
        Map<Integer, Double> topicAssignment = new HashMap<>();
        topicAssignment.put(0, 0.35);
        topicAssignment.put(2, 0.45);
        topicAssignment.put(5, 0.20);

        testDocumentRaw.setTopicAssignment(topicAssignment);
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertNotNull(doc.getTopics(), "Topics should not be null");
        assertEquals(3, doc.getTopics().size(), "Should have 3 topics");

        // Verify topic mappings
        List<DocumentSearch.SearchableTopic> topics = doc.getTopics();

        // Find topic with ID 0
        DocumentSearch.SearchableTopic topic0 = topics.stream()
                .filter(t -> t.getTopicId() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(topic0);
        assertEquals(0.35, topic0.getWeight(), 0.001);

        // Find topic with ID 2
        DocumentSearch.SearchableTopic topic2 = topics.stream()
                .filter(t -> t.getTopicId() == 2)
                .findFirst()
                .orElse(null);
        assertNotNull(topic2);
        assertEquals(0.45, topic2.getWeight(), 0.001);

        // Find topic with ID 5
        DocumentSearch.SearchableTopic topic5 = topics.stream()
                .filter(t -> t.getTopicId() == 5)
                .findFirst()
                .orElse(null);
        assertNotNull(topic5);
        assertEquals(0.20, topic5.getWeight(), 0.001);
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Null Topic Assignments")
    void testSyncMongoElastic_NullTopicAssignments() {
        // Arrange
        testDocumentRaw.setTopicAssignment(null);
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertNull(doc.getTopics(), "Topics should be null when topicAssignment is null");
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Empty Topic Assignments")
    void testSyncMongoElastic_EmptyTopicAssignments() {
        // Arrange
        testDocumentRaw.setTopicAssignment(new HashMap<>());
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertNotNull(doc.getTopics(), "Topics list should exist");
        assertTrue(doc.getTopics().isEmpty(), "Topics list should be empty");
    }

    @Test
    @DisplayName("syncMongoElastic - Preserves All Metadata Fields")
    void testSyncMongoElastic_PreservesMetadata() {
        // Arrange
        testDocumentRaw.setSpeaker_name("John Doe");
        testDocumentRaw.setSpeaker_party("Labour");
        testDocumentRaw.setSpeaker_gender("M");
        testDocumentRaw.setDate("2024-01-15");

        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertEquals("doc-001", doc.getId());
        assertEquals("Test Document", doc.getTitle());
        assertEquals("This is test content", doc.getContent());
        assertEquals("test.com", doc.getDomain());

        // Note: DocumentSearch doesn't have speaker fields, only core fields are mapped
        // This test verifies the mapping doesn't crash with extra fields in DocumentRaw
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Null Fields in DocumentRaw")
    void testSyncMongoElastic_NullFields() {
        // Arrange
        DocumentRaw docWithNulls = new DocumentRaw();
        docWithNulls.setId("doc-null");
        docWithNulls.setTitle(null);
        docWithNulls.setContent(null);
        docWithNulls.setDomain(null);
        docWithNulls.setTopicAssignment(null);

        when(mongoDocumentRepository.findAll()).thenReturn(List.of(docWithNulls));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertEquals("doc-null", doc.getId());
        assertNull(doc.getTitle());
        assertNull(doc.getContent());
        assertNull(doc.getDomain());
        assertNull(doc.getTopics());
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Large Number of Documents")
    void testSyncMongoElastic_LargeDataset() {
        // Arrange - Create 1000 documents
        List<DocumentRaw> largeDataset = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            DocumentRaw doc = new DocumentRaw();
            doc.setId("doc-" + i);
            doc.setTitle("Title " + i);
            doc.setContent("Content " + i);
            doc.setDomain("domain" + (i % 10) + ".com");
            largeDataset.add(doc);
        }

        when(mongoDocumentRepository.findAll()).thenReturn(largeDataset);

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(mongoDocumentRepository, times(1)).findAll();
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());

        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();
        assertEquals(1000, savedDocs.size(), "Should sync 1000 documents");

        // Verify first and last documents
        assertEquals("doc-1", savedDocs.get(0).getId());
        assertEquals("doc-1000", savedDocs.get(999).getId());
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Documents with Multiple Topics")
    void testSyncMongoElastic_MultipleTopics() {
        // Arrange - Create document with 10 topics
        Map<Integer, Double> topicAssignment = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            topicAssignment.put(i, 0.1 * (i + 1));
        }

        testDocumentRaw.setTopicAssignment(topicAssignment);
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertEquals(10, doc.getTopics().size(), "Should have 10 topics");

        // Verify all topics are present
        Set<Integer> topicIds = new HashSet<>();
        for (DocumentSearch.SearchableTopic topic : doc.getTopics()) {
            topicIds.add(topic.getTopicId());
        }
        assertEquals(10, topicIds.size(), "All 10 topic IDs should be unique");
        assertTrue(topicIds.containsAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Single Document")
    void testSyncMongoElastic_SingleDocument() {
        // Arrange
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(mongoDocumentRepository, times(1)).findAll();
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());

        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();
        assertEquals(1, savedDocs.size());
        assertEquals("doc-001", savedDocs.get(0).getId());
    }

    @Test
    @DisplayName("syncMongoElastic - Maintains Document Order")
    void testSyncMongoElastic_MaintainsOrder() {
        // Arrange - Create documents with specific order
        List<DocumentRaw> orderedDocs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DocumentRaw doc = new DocumentRaw();
            doc.setId("doc-" + i);
            doc.setTitle("Title " + i);
            doc.setContent("Content " + i);
            doc.setDomain("test.com");
            orderedDocs.add(doc);
        }

        when(mongoDocumentRepository.findAll()).thenReturn(orderedDocs);

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        // Verify order is maintained
        for (int i = 0; i < 5; i++) {
            assertEquals("doc-" + (i + 1), savedDocs.get(i).getId(),
                    "Document order should be maintained");
        }
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Special Characters in Content")
    void testSyncMongoElastic_SpecialCharacters() {
        // Arrange
        testDocumentRaw.setTitle("Title with \"quotes\" and 'apostrophes'");
        testDocumentRaw.setContent("Content with\nnewlines\tand\ttabs and special chars: €£¥");
        testDocumentRaw.setDomain("test-domain.com");

        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertTrue(doc.getTitle().contains("quotes"));
        assertTrue(doc.getTitle().contains("apostrophes"));
        assertTrue(doc.getContent().contains("newlines"));
        assertTrue(doc.getContent().contains("tabs"));
    }

    @Test
    @DisplayName("syncMongoElastic - Handles Topic Assignment with Zero Weight")
    void testSyncMongoElastic_ZeroWeightTopic() {
        // Arrange
        Map<Integer, Double> topicAssignment = new HashMap<>();
        topicAssignment.put(0, 0.0);
        topicAssignment.put(1, 0.5);
        topicAssignment.put(2, 0.5);

        testDocumentRaw.setTopicAssignment(topicAssignment);
        when(mongoDocumentRepository.findAll()).thenReturn(List.of(testDocumentRaw));

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        DocumentSearch doc = savedDocs.get(0);
        assertEquals(3, doc.getTopics().size(), "Should include topic with zero weight");

        // Find topic with weight 0.0
        DocumentSearch.SearchableTopic zeroTopic = doc.getTopics().stream()
                .filter(t -> t.getTopicId() == 0)
                .findFirst()
                .orElse(null);
        assertNotNull(zeroTopic);
        assertEquals(0.0, zeroTopic.getWeight(), 0.001);
    }

    @Test
    @DisplayName("syncMongoElastic - Verifies Stream-Based Conversion")
    void testSyncMongoElastic_StreamConversion() {
        // Arrange
        List<DocumentRaw> docs = Arrays.asList(
                createDocumentRaw("doc-1", "Title 1", "Content 1"),
                createDocumentRaw("doc-2", "Title 2", "Content 2"),
                createDocumentRaw("doc-3", "Title 3", "Content 3")
        );

        when(mongoDocumentRepository.findAll()).thenReturn(docs);

        // Act
        indexService.syncMongoElastic();

        // Assert
        verify(elasticDocumentRepository, times(1)).saveAll(searchDocumentListCaptor.capture());
        List<DocumentSearch> savedDocs = searchDocumentListCaptor.getValue();

        // Verify all documents are converted
        assertEquals(3, savedDocs.size());
        assertEquals("doc-1", savedDocs.get(0).getId());
        assertEquals("doc-2", savedDocs.get(1).getId());
        assertEquals("doc-3", savedDocs.get(2).getId());
    }

    // ==================== Helper Methods ====================

    private DocumentRaw createDocumentRaw(String id, String title, String content) {
        DocumentRaw doc = new DocumentRaw();
        doc.setId(id);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setDomain("test.com");
        return doc;
    }
}