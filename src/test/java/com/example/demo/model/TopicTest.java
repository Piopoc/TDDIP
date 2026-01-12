package com.example.demo.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Topic Test")
class TopicTest {

    @Test
    @DisplayName("Set and get correct values")
    void testGettersAndSetters() {
        Topic topic  = new Topic();
        Integer expectedID = 10;
        double expectedWeight = 0.55;
        List<String> expectedWords = Arrays.asList("politics", "election", "vote");

        topic.setTopicId(expectedID);
        topic.setWeight(expectedWeight);
        topic.setTopWords(expectedWords);

        assertEquals(expectedID, topic.getTopicId());
        assertEquals(expectedWeight, topic.getWeight(), 0.0001);
        assertEquals(expectedWords, topic.getTopWords());
        }

    @Test
    @DisplayName("Should preserve the order of the topWords")
    void testTopWordsOrder() {
        Topic topic = new Topic();
        List<String> orderedWords = Arrays.asList("very_important", "quite_important", "low_important");

        topic.setTopWords(orderedWords);

       List<String> foundWords = topic.getTopWords();
       assertNotNull(foundWords);
       assertEquals("very_important", foundWords.get(0));
       assertEquals("quite_important", foundWords.get(1));
       assertEquals("low_important", foundWords.get(2));
    }

    @Test
    @DisplayName("Should verify equals and hashCode contract")
    void testEqualsAndHashCode() {
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

        assertEquals(topic1, topic2, "Objects with same data should be equal");
        assertEquals(topic1.hashCode(), topic2.hashCode(), "HashCodes should match");
        assertNotEquals(topic1, topic3, "Objects with different data should not be equal");
    }

    @Test
    @DisplayName("Should return a valid string representation")
    void testToString() {
        Topic topic = new Topic();
        topic.setTopicId(99);
        topic.setTopWords(List.of("java", "test"));

        String result = topic.toString();

        assertNotNull(result);
        assertTrue(result.contains("99"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("Topic"));
    }

    @Test
    @DisplayName("Should handle empty list of words")
    void testTopWordsEmpty() {
        Topic topic = new Topic();
        topic.setTopWords(List.of());

        assertNotNull(topic.getTopWords());
        assertTrue(topic.getTopWords().isEmpty());
    }
}

