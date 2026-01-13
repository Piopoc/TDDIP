package com.example.demo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Topic model class.
 * Tests cover getter/setter functionality, Lombok-generated methods,
 * edge cases, and JSON serialization/deserialization.
 */
@DisplayName("Topic Model Tests")
class TopicTest {

    // ==================== Getter/Setter Tests ====================

    @Test
    @DisplayName("Getter/Setter - Set and get correct values")
    void testGettersAndSetters() {
        // Arrange
        Topic topic = new Topic();
        Integer expectedID = 10;
        double expectedWeight = 0.55;
        List<String> expectedWords = Arrays.asList("politics", "election", "vote");

        // Act
        topic.setTopicId(expectedID);
        topic.setWeight(expectedWeight);
        topic.setTopWords(expectedWords);

        // Assert - Verify all fields are correctly set and retrieved
        assertEquals(expectedID, topic.getTopicId());
        assertEquals(expectedWeight, topic.getWeight(), 0.0001);
        assertEquals(expectedWords, topic.getTopWords());
    }

    // ==================== TopWords List Tests ====================

    @Test
    @DisplayName("TopWords - Should preserve the order of words")
    void testTopWordsOrder() {
        // Arrange - Create topic with ordered words (most to least important)
        Topic topic = new Topic();
        List<String> orderedWords = Arrays.asList("very_important", "quite_important", "low_important");

        // Act
        topic.setTopWords(orderedWords);

        // Assert - Verify the order is preserved after retrieval
        List<String> foundWords = topic.getTopWords();
        assertNotNull(foundWords);
        assertEquals("very_important", foundWords.get(0), "First word should be 'very_important'");
        assertEquals("quite_important", foundWords.get(1), "Second word should be 'quite_important'");
        assertEquals("low_important", foundWords.get(2), "Third word should be 'low_important'");
    }

    @Test
    @DisplayName("TopWords - Should handle empty list")
    void testTopWordsEmpty() {
        // Arrange
        Topic topic = new Topic();

        // Act - Set empty list
        topic.setTopWords(List.of());

        // Assert - Verify empty list is handled correctly
        assertNotNull(topic.getTopWords(), "TopWords should not be null");
        assertTrue(topic.getTopWords().isEmpty(), "TopWords should be empty");
    }

    @Test
    @DisplayName("TopWords - Should handle null value")
    void testTopWordsNull() {
        // Arrange
        Topic topic = new Topic();

        // Act - Set null
        topic.setTopWords(null);

        // Assert - Verify null is accepted
        assertNull(topic.getTopWords(), "TopWords should be null");
    }

    @Test
    @DisplayName("TopWords - Should handle large list (100 words)")
    void testLargeTopWordsList() {
        // Arrange - Create topic with 100 words
        Topic topic = new Topic();
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            words.add("word" + i);
        }

        // Act
        topic.setTopWords(words);

        // Assert - Verify all words are stored correctly
        assertEquals(100, topic.getTopWords().size(), "Should contain 100 words");
        assertEquals("word0", topic.getTopWords().get(0), "First word should be 'word0'");
        assertEquals("word99", topic.getTopWords().get(99), "Last word should be 'word99'");
    }

    // ==================== Weight Tests ====================

    @Test
    @DisplayName("Weight - Should accept negative value")
    void testNegativeWeight() {
        // Arrange
        Topic topic = new Topic();

        // Act - Set negative weight
        topic.setWeight(-0.5);

        // Assert - Verify negative weight is accepted
        assertEquals(-0.5, topic.getWeight(), 0.0001, "Should accept negative weight");
    }

    @Test
    @DisplayName("Weight - Should handle zero value")
    void testZeroWeight() {
        // Arrange
        Topic topic = new Topic();

        // Act - Set zero weight
        topic.setWeight(0.0);

        // Assert - Verify zero weight is handled correctly
        assertEquals(0.0, topic.getWeight(), 0.0001, "Should accept zero weight");
    }

    // ==================== Lombok Generated Methods Tests ====================

    @Test
    @DisplayName("Lombok @Data - Should verify equals() and hashCode() contract")
    void testEqualsAndHashCode() {
        // Arrange - Create three topics: two identical, one different
        List<String> words = List.of("data", "analysis");

        Topic topic1 = new Topic();
        topic1.setTopicId(1);
        topic1.setWeight(0.2);
        topic1.setTopWords(words);

        Topic topic2 = new Topic();
        topic2.setTopicId(1);
        topic2.setWeight(0.2);
        topic2.setTopWords(words);

        Topic topic3 = new Topic();
        topic3.setTopicId(2);
        topic3.setWeight(0.2);
        topic3.setTopWords(words);

        // Assert - Verify equals() and hashCode() contract
        assertEquals(topic1, topic2, "Topics with identical values should be equal");
        assertEquals(topic1.hashCode(), topic2.hashCode(), "Equal topics should have same hashCode");
        assertNotEquals(topic1, topic3, "Topics with different topicId should not be equal");
    }

    @Test
    @DisplayName("Lombok @Data - Should return valid toString() representation")
    void testToString() {
        // Arrange
        Topic topic = new Topic();
        topic.setTopicId(99);
        topic.setTopWords(List.of("java", "test"));

        // Act
        String result = topic.toString();

        // Assert - Verify toString contains key information
        assertNotNull(result, "toString() should not return null");
        assertTrue(result.contains("99"), "toString() should contain topicId");
        assertTrue(result.contains("java"), "toString() should contain topWords");
        assertTrue(result.contains("Topic"), "toString() should contain class name");
    }

    // ==================== JSON Serialization Tests ====================

    @Test
    @DisplayName("JSON Serialization - Should serialize core fields correctly")
    void testJsonSerialization() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();

        Topic topic = new Topic();
        topic.setTopicId(5);
        topic.setWeight(0.75);
        topic.setTopWords(List.of("word1", "word2", "word3"));

        // Act - Serialize Topic to JSON
        String json = mapper.writeValueAsString(topic);

        // Assert - Verify JSON contains all fields
        assertTrue(json.contains("\"topicId\":5"), "JSON should contain topicId");
        assertTrue(json.contains("\"weight\":0.75"), "JSON should contain weight");
        assertTrue(json.contains("word1"), "JSON should contain topWords");
    }

    @Test
    @DisplayName("JSON Deserialization - Should deserialize core fields correctly")
    void testJsonDeserialization() throws Exception {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"topicId\":5,\"weight\":0.75,\"topWords\":[\"word1\",\"word2\"]}";

        // Act - Deserialize JSON to Topic object
        Topic topic = mapper.readValue(json, Topic.class);

        // Assert - Verify all fields are correctly deserialized
        assertEquals(5, topic.getTopicId(), "topicId should be 5");
        assertEquals(0.75, topic.getWeight(), 0.001, "weight should be 0.75");
        assertEquals(2, topic.getTopWords().size(), "Should have 2 topWords");
        assertEquals("word1", topic.getTopWords().get(0), "First word should be 'word1'");
    }
}