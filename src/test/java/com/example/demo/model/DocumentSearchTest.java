package com.example.demo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DocumentSearch model class.
 * Tests cover getter/setter functionality, inner class SearchableTopic,
 * Lombok-generated methods, edge cases, and JSON serialization/deserialization.
 * This class represents documents indexed in Elasticsearch for full-text search.
 */
@DisplayName("DocumentSearch Model Tests")
class DocumentSearchTest {

    // ==================== Getter/Setter Tests ====================

    /**
     * Verify that all core fields can be correctly set and retrieved.
     * Tests id, title, content, and domain fields.
     */
    @Test
    @DisplayName("Getter/Setter - Should set and get values correctly")
    void testGettersAndSetters() {
        // Arrange
        DocumentSearch document = new DocumentSearch();
        String expectedId = "doc-123";
        String expectedTitle = "Test Title";
        String expectedContent = "This is some content";
        String expectedDomain = "example.com";

        // Act
        document.setId(expectedId);
        document.setTitle(expectedTitle);
        document.setContent(expectedContent);
        document.setDomain(expectedDomain);

        // Assert - Verify all fields are correctly set and retrieved
        assertEquals(expectedId, document.getId());
        assertEquals(expectedTitle, document.getTitle());
        assertEquals(expectedContent, document.getContent());
        assertEquals(expectedDomain, document.getDomain());
    }

    // ==================== SearchableTopic Inner Class Tests ====================

    /**
     * Verify that the inner class SearchableTopic correctly handles
     * getter and setter methods inherited from Lombok @Data annotation.
     */
    @Test
    @DisplayName("SearchableTopic - Should handle getter/setter correctly")
    void testSearchableTopic() {
        // Arrange
        DocumentSearch.SearchableTopic topic = new DocumentSearch.SearchableTopic();
        int expectedId = 5;
        double expectedWeight = 0.85;

        // Act
        topic.setTopicId(expectedId);
        topic.setWeight(expectedWeight);

        // Assert - Verify fields are correctly set
        assertEquals(expectedId, topic.getTopicId());
        // 0.0001 is the fault tolerance for comparing decimal values
        assertEquals(expectedWeight, topic.getWeight(), 0.0001);
    }

    /**
     * Verify that SearchableTopic correctly implements equals() and hashCode()
     * contract for comparing topic objects.
     */
    @Test
    @DisplayName("SearchableTopic - Should verify equals and hashCode")
    void testSearchableTopic_EqualsAndHashCode() {
        // Arrange - Create three topics: two identical, one different
        DocumentSearch.SearchableTopic topic1 = new DocumentSearch.SearchableTopic();
        topic1.setTopicId(1);
        topic1.setWeight(0.5);

        DocumentSearch.SearchableTopic topic2 = new DocumentSearch.SearchableTopic();
        topic2.setTopicId(1);
        topic2.setWeight(0.5);

        DocumentSearch.SearchableTopic topic3 = new DocumentSearch.SearchableTopic();
        topic3.setTopicId(2);
        topic3.setWeight(0.5);

        // Assert - Verify equals() and hashCode() contract
        assertEquals(topic1, topic2, "Topics with identical values should be equal");
        assertEquals(topic1.hashCode(), topic2.hashCode(), "Equal topics should have same hashCode");
        assertNotEquals(topic1, topic3, "Topics with different topicId should not be equal");
    }

    // ==================== Topics List Tests ====================

    /**
     * Verify that the document correctly handles a list of associated topics.
     * Tests that multiple topics can be added and retrieved in order.
     */
    @Test
    @DisplayName("Topics List - Should handle multiple topics correctly")
    void testTopicsList() {
        // Arrange
        DocumentSearch document = new DocumentSearch();

        DocumentSearch.SearchableTopic topic1 = new DocumentSearch.SearchableTopic();
        topic1.setTopicId(1);
        topic1.setWeight(0.5);

        DocumentSearch.SearchableTopic topic2 = new DocumentSearch.SearchableTopic();
        topic2.setTopicId(2);
        topic2.setWeight(0.3);

        List<DocumentSearch.SearchableTopic> topics = new ArrayList<>();
        topics.add(topic1);
        topics.add(topic2);

        // Act
        document.setTopics(topics);

        // Assert - Verify list is stored correctly with proper order
        assertNotNull(document.getTopics());
        assertEquals(2, document.getTopics().size());
        assertEquals(1, document.getTopics().get(0).getTopicId());
        assertEquals(2, document.getTopics().get(1).getTopicId());
    }

    /**
     * Verify that the document correctly handles an empty topics list.
     */
    @Test
    @DisplayName("Topics List - Should handle empty list")
    void testTopics_EmptyList() {
        // Arrange
        DocumentSearch document = new DocumentSearch();

        // Act
        document.setTopics(new ArrayList<>());

        // Assert - Verify empty list is handled correctly
        assertNotNull(document.getTopics(), "Topics list should not be null");
        assertTrue(document.getTopics().isEmpty(), "Topics list should be empty");
    }

    /**
     * Verify that the document correctly handles null topics list.
     */
    @Test
    @DisplayName("Topics List - Should handle null list")
    void testTopics_NullList() {
        // Arrange
        DocumentSearch document = new DocumentSearch();

        // Act
        document.setTopics(null);

        // Assert - Verify null is accepted
        assertNull(document.getTopics(), "Topics list should be null");
    }

    /**
     * Verify that the document can handle a large number of topics (100).
     * Tests scalability and proper indexing.
     */
    @Test
    @DisplayName("Topics List - Should handle large list (100 topics)")
    void testTopics_LargeList() {
        // Arrange - Create document with 100 topics
        DocumentSearch document = new DocumentSearch();
        List<DocumentSearch.SearchableTopic> topics = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            DocumentSearch.SearchableTopic topic = new DocumentSearch.SearchableTopic();
            topic.setTopicId(i);
            topic.setWeight(0.01 * i);
            topics.add(topic);
        }

        // Act
        document.setTopics(topics);

        // Assert - Verify all topics are stored correctly
        assertEquals(100, document.getTopics().size(), "Should contain 100 topics");
        assertEquals(0, document.getTopics().get(0).getTopicId(), "First topic should have ID 0");
        assertEquals(99, document.getTopics().get(99).getTopicId(), "Last topic should have ID 99");
    }

    // ==================== Lombok Generated Methods Tests ====================

    /**
     * Verify that Lombok-generated equals() and hashCode() methods
     * correctly implement the equality contract.
     */
    @Test
    @DisplayName("Lombok @Data - Should verify equals and hashCode")
    void testEqualsAndHashCode() {
        // Arrange - Create three documents: two identical, one different
        DocumentSearch doc1 = new DocumentSearch();
        doc1.setId("1");
        doc1.setTitle("Same Title");

        DocumentSearch doc2 = new DocumentSearch();
        doc2.setId("1");
        doc2.setTitle("Same Title");

        DocumentSearch doc3 = new DocumentSearch();
        doc3.setId("2");
        doc3.setTitle("Different Title");

        // Assert - Verify reflexive, symmetric, and transitive properties
        assertEquals(doc1, doc1, "Document should equal itself (reflexive)");
        assertEquals(doc1, doc2, "Documents with identical values should be equal");
        assertEquals(doc2, doc1, "Equality should be symmetric");
        assertEquals(doc1.hashCode(), doc2.hashCode(), "Equal documents should have same hashCode");
        assertNotEquals(doc1, doc3, "Documents with different values should not be equal");
    }

    /**
     * Verify that Lombok-generated toString() method returns a valid
     * string representation containing key document information.
     */
    @Test
    @DisplayName("Lombok @Data - Should return valid toString representation")
    void testToString() {
        // Arrange
        DocumentSearch document = new DocumentSearch();
        document.setId("100");
        document.setTitle("ToString Test");

        // Act
        String result = document.toString();

        // Assert - Verify toString contains key information
        assertNotNull(result, "toString() should not return null");
        assertTrue(result.contains("100"), "toString() should contain document ID");
        assertTrue(result.contains("ToString Test"), "toString() should contain document title");
        assertTrue(result.contains("DocumentSearch"), "toString() should contain class name");
    }

    // ==================== Edge Cases - Domain Field ====================

    /**
     * Verify that the domain field correctly handles complex domain formats
     * including subdomains and country-specific TLDs.
     */
    @Test
    @DisplayName("Domain - Should handle complex domain formats")
    void testDomain_ComplexFormat() {
        // Arrange
        DocumentSearch document = new DocumentSearch();
        String complexDomain = "news.subdomain.example.co.uk";

        // Act
        document.setDomain(complexDomain);

        // Assert - Verify complex domain is stored correctly
        assertEquals("news.subdomain.example.co.uk", document.getDomain());
    }

    // ==================== Edge Cases - Content Field ====================

    /**
     * Verify that the content field can handle very long text (32,000+ chars).
     * Tests scalability for large documents.
     */
    @Test
    @DisplayName("Content - Should handle very long text")
    void testContent_LongText() {
        // Arrange - Create content with 32,000+ characters
        DocumentSearch document = new DocumentSearch();
        String longContent = "This is a long article content. ".repeat(1000);

        // Act
        document.setContent(longContent);

        // Assert - Verify long content is stored without truncation
        assertEquals(longContent, document.getContent());
        assertTrue(document.getContent().length() > 1000, "Content should retain its full length");
    }

    /**
     * Verify that the content field correctly handles special characters,
     * symbols, and Unicode emojis.
     */
    @Test
    @DisplayName("Content - Should handle special characters")
    void testContent_SpecialCharacters() {
        // Arrange
        DocumentSearch document = new DocumentSearch();
        String specialContent = "Text with symbols: @#€%&/()=?^ and emojis \uD83D\uDE00";

        // Act
        document.setContent(specialContent);

        // Assert - Verify special characters are preserved
        assertEquals(specialContent, document.getContent());
    }

    // ==================== Edge Cases - Null and Empty Values ====================

    /**
     * Verify that optional fields correctly accept null values.
     */
    @Test
    @DisplayName("Fields - Should allow null values for optional fields")
    void testFields_NullValues() {
        // Arrange
        DocumentSearch document = new DocumentSearch();

        // Act
        document.setDomain(null);
        document.setContent(null);

        // Assert - Verify null is accepted
        assertNull(document.getDomain());
        assertNull(document.getContent());
    }

    /**
     * Verify that all fields correctly handle empty strings.
     */
    @Test
    @DisplayName("Fields - Should handle empty strings")
    void testFields_EmptyStrings() {
        // Arrange
        DocumentSearch document = new DocumentSearch();

        // Act
        document.setId("");
        document.setTitle("");
        document.setContent("");
        document.setDomain("");

        // Assert - Verify empty strings are stored correctly
        assertEquals("", document.getId());
        assertEquals("", document.getTitle());
        assertEquals("", document.getContent());
        assertEquals("", document.getDomain());
    }

    // ==================== JSON Serialization Tests ====================

    /**
     * Verify that DocumentSearch objects can be correctly serialized to JSON.
     * Tests that all fields including nested topics are properly converted.
     */
    @Test
    @DisplayName("JSON Serialization - Should serialize correctly")
    void testJsonSerialization() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();

        DocumentSearch document = new DocumentSearch();
        document.setId("doc-001");
        document.setTitle("Test");
        document.setContent("Content");
        document.setDomain("test.com");

        DocumentSearch.SearchableTopic topic = new DocumentSearch.SearchableTopic();
        topic.setTopicId(5);
        topic.setWeight(0.75);
        document.setTopics(List.of(topic));

        // Act - Serialize to JSON
        String json = mapper.writeValueAsString(document);

        // Assert - Verify JSON contains all fields
        assertTrue(json.contains("\"id\":\"doc-001\""), "JSON should contain document ID");
        assertTrue(json.contains("\"title\":\"Test\""), "JSON should contain title");
        assertTrue(json.contains("\"topicId\":5"), "JSON should contain nested topic ID");
    }

    /**
     * Verify that JSON can be correctly deserialized into DocumentSearch objects.
     * Tests that all fields including nested topics are properly reconstructed.
     */
    @Test
    @DisplayName("JSON Deserialization - Should deserialize correctly")
    void testJsonDeserialization() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":\"doc-001\",\"title\":\"Test\",\"content\":\"Content\"," +
                "\"domain\":\"test.com\",\"topics\":[{\"topicId\":5,\"weight\":0.75}]}";

        // Act - Deserialize from JSON
        DocumentSearch document = mapper.readValue(json, DocumentSearch.class);

        // Assert - Verify all fields are correctly deserialized
        assertEquals("doc-001", document.getId(), "ID should be correctly deserialized");
        assertEquals("Test", document.getTitle(), "Title should be correctly deserialized");
        assertEquals(1, document.getTopics().size(), "Should have 1 topic");
        assertEquals(5, document.getTopics().get(0).getTopicId(), "Topic ID should be correctly deserialized");
    }
}