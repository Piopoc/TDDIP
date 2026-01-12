package com.example.demo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentSearch Model Tests")
class DocumentSearchTest {

    /**
    Verify the correct save of the fields
     **/
    @Test
    @DisplayName("Should set and get values correctly")
    void testGettersAndSetters() {
        DocumentSearch document = new DocumentSearch();
        String expectedId = "doc-123";
        String expectedTitle = "Test Title";
        String expectedContent = "This is some content";
        String expectedDomain = "example.com";

        document.setId(expectedId);
        document.setTitle(expectedTitle);
        document.setContent(expectedContent);
        document.setDomain(expectedDomain);

        assertEquals(expectedId, document.getId());
        assertEquals(expectedTitle, document.getTitle());
        assertEquals(expectedContent, document.getContent());
        assertEquals(expectedDomain, document.getDomain());
    }

    /**
    Verify the correct working of getter and setter methods inherited from Lombok
    thanks to @Data
     **/
    @Test
    @DisplayName("Should handle the inner class SearchableTopic")
    void testSearchableTopic() {
        DocumentSearch.SearchableTopic topic = new DocumentSearch.SearchableTopic();
        int expectedId = 5;
        double expectedWeight = 0.85;

        topic.setTopicId(expectedId);
        topic.setWeight(expectedWeight);

        assertEquals(expectedId, topic.getTopicId());
        //0.0001 is the fault tollerance of the comparison of the two decimal values
        assertEquals(expectedWeight, topic.getWeight(), 0.0001);
    }


    @Test
    @DisplayName("Should handle the list of topics correctly")
    void testTopicsList() {
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

        document.setTopics(topics);

        assertNotNull(document.getTopics());
        assertEquals(2, document.getTopics().size());
        assertEquals(1, document.getTopics().get(0).getTopicId());
        assertEquals(2, document.getTopics().get(1).getTopicId());
    }

    @Test
    @DisplayName("Should verify equals and hashCode")
    void testEqualsAndHashCode() {
        DocumentSearch doc1 = new DocumentSearch();
        doc1.setId("1");
        doc1.setTitle("Same Title");

        DocumentSearch doc2 = new DocumentSearch();
        doc2.setId("1");
        doc2.setTitle("Same Title");

        DocumentSearch doc3 = new DocumentSearch();
        doc3.setId("2");
        doc3.setTitle("Different Title");

        assertEquals(doc1, doc1);

        assertEquals(doc1, doc2);
        assertEquals(doc2, doc1);
        assertEquals(doc1.hashCode(), doc2.hashCode());


        assertNotEquals(doc1, doc3);
    }

    @Test
    @DisplayName("Should return a valid string representation (toString)")
    void testToString() {

        DocumentSearch document = new DocumentSearch();
        document.setId("100");
        document.setTitle("ToString Test");

        String result = document.toString();

        assertNotNull(result);
        assertTrue(result.contains("100"));
        assertTrue(result.contains("ToString Test"));

        assertTrue(result.contains("DocumentSearch"));
    }

    @Test
    @DisplayName("Should handle complex domain formats")
    void testDomain_ComplexFormat() {
        DocumentSearch document = new DocumentSearch();
        String complexDomain = "news.subdomain.example.co.uk";

        document.setDomain(complexDomain);

        assertEquals("news.subdomain.example.co.uk", document.getDomain());
    }

    @Test
    @DisplayName("Should handle very long content text")
    void testContent_LongText() {
        DocumentSearch document = new DocumentSearch();
        String longContent = "This is a long article content. ".repeat(1000);

        document.setContent(longContent);

        assertEquals(longContent, document.getContent());
        assertTrue(document.getContent().length() > 1000, "Content should retain its full length");
    }

    @Test
    @DisplayName("Should allow null values for optional fields")
    void testFields_NullValues() {
        DocumentSearch document = new DocumentSearch();

        document.setDomain(null);
        document.setContent(null);

        assertNull(document.getDomain());
        assertNull(document.getContent());
    }

    @Test
    @DisplayName("Should handle special characters in content")
    void testContent_SpecialCharacters() {
        DocumentSearch document = new DocumentSearch();
        String specialContent = "Text with symbols: @#€%&/()=?^ and emojis \uD83D\uDE00";

        document.setContent(specialContent);

        assertEquals(specialContent, document.getContent());
    }
}