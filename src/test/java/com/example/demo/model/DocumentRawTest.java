package com.example.demo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentRaw Model Tests")
class DocumentRawTest {

    private ObjectMapper objectMapper;
    private DocumentRaw documentRaw;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        documentRaw = new DocumentRaw();
    }

    // ==================== Tests for Basic Fields (Getter/Setter) ====================

    @Test
    @DisplayName("Getter/Setter - Core Fields")
    void testGettersSetters_CoreFields() {
        // Arrange & Act
        documentRaw.setId("doc-001");
        documentRaw.setDomain("test.com");
        documentRaw.setTitle("Test Title");
        documentRaw.setContent("Test Content");
        documentRaw.setDate("2024-01-15");

        // Assert
        assertEquals("doc-001", documentRaw.getId());
        assertEquals("test.com", documentRaw.getDomain());
        assertEquals("Test Title", documentRaw.getTitle());
        assertEquals("Test Content", documentRaw.getContent());
        assertEquals("2024-01-15", documentRaw.getDate());
    }

    @Test
    @DisplayName("Getter/Setter - ParlaMint Speaker Fields")
    void testGettersSetters_SpeakerFields() {
        // Arrange & Act
        documentRaw.setSpeaker_name("John Doe");
        documentRaw.setSpeaker_party("Labour");
        documentRaw.setSpeaker_party_name("Labour Party");
        documentRaw.setSpeaker_gender("M");
        documentRaw.setSpeaker_role("MP");
        documentRaw.setSpeaker_id("john-001");
        documentRaw.setSpeaker_mp("true");
        documentRaw.setSpeaker_birth("1970");
        documentRaw.setSpeaker_minister("false");

        // Assert
        assertEquals("John Doe", documentRaw.getSpeaker_name());
        assertEquals("Labour", documentRaw.getSpeaker_party());
        assertEquals("Labour Party", documentRaw.getSpeaker_party_name());
        assertEquals("M", documentRaw.getSpeaker_gender());
        assertEquals("MP", documentRaw.getSpeaker_role());
        assertEquals("john-001", documentRaw.getSpeaker_id());
        assertEquals("true", documentRaw.getSpeaker_mp());
        assertEquals("1970", documentRaw.getSpeaker_birth());
        assertEquals("false", documentRaw.getSpeaker_minister());
    }

    @Test
    @DisplayName("Getter/Setter - ParlaMint Party and Session Fields")
    void testGettersSetters_PartyAndSessionFields() {
        // Arrange & Act
        documentRaw.setParty_status("Coalition");
        documentRaw.setParty_orientation("Centre-left");
        documentRaw.setSubcorpus("Reference");
        documentRaw.setSession("Session-01");
        documentRaw.setBody("Commons");
        documentRaw.setAgenda("item-01");
        documentRaw.setTopic("Economics");
        documentRaw.setMeeting("meeting-01");
        documentRaw.setSitting("sitting-01");
        documentRaw.setTerm("2019-2024");
        documentRaw.setLang("en");

        // Assert
        assertEquals("Coalition", documentRaw.getParty_status());
        assertEquals("Centre-left", documentRaw.getParty_orientation());
        assertEquals("Reference", documentRaw.getSubcorpus());
        assertEquals("Session-01", documentRaw.getSession());
        assertEquals("Commons", documentRaw.getBody());
        assertEquals("item-01", documentRaw.getAgenda());
        assertEquals("Economics", documentRaw.getTopic());
        assertEquals("meeting-01", documentRaw.getMeeting());
        assertEquals("sitting-01", documentRaw.getSitting());
        assertEquals("2019-2024", documentRaw.getTerm());
        assertEquals("en", documentRaw.getLang());
    }

    @Test
    @DisplayName("Getter/Setter - Topic Assignment Map")
    void testGettersSetters_TopicAssignment() {
        // Arrange
        Map<Integer, Double> topicAssignment = new HashMap<>();
        topicAssignment.put(0, 0.35);
        topicAssignment.put(2, 0.45);
        topicAssignment.put(5, 0.20);

        // Act
        documentRaw.setTopicAssignment(topicAssignment);

        // Assert
        assertNotNull(documentRaw.getTopicAssignment());
        assertEquals(3, documentRaw.getTopicAssignment().size());
        assertEquals(0.35, documentRaw.getTopicAssignment().get(0), 0.001);
        assertEquals(0.45, documentRaw.getTopicAssignment().get(2), 0.001);
        assertEquals(0.20, documentRaw.getTopicAssignment().get(5), 0.001);
    }

    @Test
    @DisplayName("Getter/Setter - Null Values")
    void testGettersSetters_NullValues() {
        // Act
        documentRaw.setId(null);
        documentRaw.setTitle(null);
        documentRaw.setContent(null);
        documentRaw.setTopicAssignment(null);

        // Assert
        assertNull(documentRaw.getId());
        assertNull(documentRaw.getTitle());
        assertNull(documentRaw.getContent());
        assertNull(documentRaw.getTopicAssignment());
    }

    // ==================== Tests for JSON Serialization ====================

    @Test
    @DisplayName("JSON Serialization - Core Fields")
    void testJsonSerialization_CoreFields() throws Exception {
        // Arrange
        documentRaw.setId("doc-001");
        documentRaw.setDomain("test.com");
        documentRaw.setTitle("Test Title");
        documentRaw.setContent("Test Content");
        documentRaw.setDate("2024-01-15");

        // Act
        String json = objectMapper.writeValueAsString(documentRaw);

        // Assert
        assertTrue(json.contains("\"id\":\"doc-001\""));
        assertTrue(json.contains("\"domain\":\"test.com\""));
        assertTrue(json.contains("\"title\":\"Test Title\""));
        assertTrue(json.contains("\"content\":\"Test Content\""));
        assertTrue(json.contains("\"date\":\"2024-01-15\""));
    }

    @Test
    @DisplayName("JSON Serialization - With Topic Assignment")
    void testJsonSerialization_WithTopicAssignment() throws Exception {
        // Arrange
        documentRaw.setId("doc-001");
        Map<Integer, Double> topicAssignment = new HashMap<>();
        topicAssignment.put(0, 0.5);
        topicAssignment.put(1, 0.3);
        documentRaw.setTopicAssignment(topicAssignment);

        // Act
        String json = objectMapper.writeValueAsString(documentRaw);

        // Assert
        assertTrue(json.contains("\"topicAssignment\""));
        assertTrue(json.contains("\"0\":0.5") || json.contains("\"0\":0.5"));
        assertTrue(json.contains("\"1\":0.3") || json.contains("\"1\":0.3"));
    }

    @Test
    @DisplayName("JSON Serialization - With ParlaMint Metadata")
    void testJsonSerialization_ParlaMintMetadata() throws Exception {
        // Arrange
        documentRaw.setId("speech-001");
        documentRaw.setSpeaker_name("John Doe");
        documentRaw.setSpeaker_party("Labour");
        documentRaw.setSpeaker_gender("M");

        // Act
        String json = objectMapper.writeValueAsString(documentRaw);

        // Assert
        assertTrue(json.contains("\"speaker_name\":\"John Doe\""));
        assertTrue(json.contains("\"speaker_party\":\"Labour\""));
        assertTrue(json.contains("\"speaker_gender\":\"M\""));
    }

    // ==================== Tests for JSON Deserialization ====================

    @Test
    @DisplayName("JSON Deserialization - Core Fields")
    void testJsonDeserialization_CoreFields() throws Exception {
        // Arrange
        String json = "{\"id\":\"doc-001\",\"domain\":\"test.com\",\"title\":\"Test Title\"," +
                "\"content\":\"Test Content\",\"date\":\"2024-01-15\"}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals("doc-001", result.getId());
        assertEquals("test.com", result.getDomain());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("2024-01-15", result.getDate());
    }

    @Test
    @DisplayName("JSON Deserialization - @JsonAlias 'text' to 'content'")
    void testJsonDeserialization_JsonAliasTextToContent() throws Exception {
        // Arrange - JSON with "text" field
        String json = "{\"id\":\"doc-001\",\"domain\":\"test.com\",\"title\":\"Test Title\"," +
                "\"text\":\"This is the text content\",\"date\":\"2024-01-15\"}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals("This is the text content", result.getContent(),
                "@JsonAlias should map 'text' field to 'content' property");
    }

    @Test
    @DisplayName("JSON Deserialization - Both 'text' and 'content' present")
    void testJsonDeserialization_TextAndContentBothPresent() throws Exception {
        // Arrange - JSON with both "text" and "content"
        // "content" should take precedence over "text" alias
        String json = "{\"id\":\"doc-001\",\"text\":\"Text value\",\"content\":\"Content value\"}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        // When both are present, the actual field name takes precedence
        assertEquals("Content value", result.getContent());
    }

    @Test
    @DisplayName("JSON Deserialization - With Topic Assignment")
    void testJsonDeserialization_WithTopicAssignment() throws Exception {
        // Arrange
        String json = "{\"id\":\"doc-001\",\"domain\":\"test.com\",\"title\":\"Test\"," +
                "\"content\":\"Content\",\"topicAssignment\":{\"0\":0.35,\"2\":0.45,\"5\":0.20}}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertNotNull(result.getTopicAssignment());
        assertEquals(3, result.getTopicAssignment().size());
        assertEquals(0.35, result.getTopicAssignment().get(0), 0.001);
        assertEquals(0.45, result.getTopicAssignment().get(2), 0.001);
        assertEquals(0.20, result.getTopicAssignment().get(5), 0.001);
    }

    @Test
    @DisplayName("JSON Deserialization - With ParlaMint Metadata")
    void testJsonDeserialization_ParlaMintMetadata() throws Exception {
        // Arrange
        String json = "{\"id\":\"speech-001\",\"domain\":\"clarin.si\",\"title\":\"Speech\"," +
                "\"text\":\"Content\",\"date\":\"2024-01-15\"," +
                "\"speaker_name\":\"John Doe\",\"speaker_party\":\"Labour\"," +
                "\"speaker_party_name\":\"Labour Party\",\"speaker_gender\":\"M\"," +
                "\"speaker_role\":\"MP\",\"speaker_id\":\"john-001\",\"speaker_mp\":\"true\"," +
                "\"speaker_birth\":\"1970\",\"speaker_minister\":\"false\"," +
                "\"party_status\":\"Coalition\",\"party_orientation\":\"Centre-left\"," +
                "\"subcorpus\":\"Reference\",\"session\":\"Session-01\",\"body\":\"Commons\"," +
                "\"agenda\":\"item-01\",\"topic\":\"Economics\",\"meeting\":\"meeting-01\"," +
                "\"sitting\":\"sitting-01\",\"term\":\"2019-2024\",\"lang\":\"en\"}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals("speech-001", result.getId());
        assertEquals("John Doe", result.getSpeaker_name());
        assertEquals("Labour", result.getSpeaker_party());
        assertEquals("Labour Party", result.getSpeaker_party_name());
        assertEquals("M", result.getSpeaker_gender());
        assertEquals("MP", result.getSpeaker_role());
        assertEquals("john-001", result.getSpeaker_id());
        assertEquals("true", result.getSpeaker_mp());
        assertEquals("1970", result.getSpeaker_birth());
        assertEquals("false", result.getSpeaker_minister());
        assertEquals("Coalition", result.getParty_status());
        assertEquals("Centre-left", result.getParty_orientation());
        assertEquals("Reference", result.getSubcorpus());
        assertEquals("Session-01", result.getSession());
        assertEquals("Commons", result.getBody());
        assertEquals("item-01", result.getAgenda());
        assertEquals("Economics", result.getTopic());
        assertEquals("meeting-01", result.getMeeting());
        assertEquals("sitting-01", result.getSitting());
        assertEquals("2019-2024", result.getTerm());
        assertEquals("en", result.getLang());
    }

    @Test
    @DisplayName("JSON Deserialization - Unknown Fields Ignored")
    void testJsonDeserialization_UnknownFieldsIgnored() throws Exception {
        // Arrange - JSON with unknown field
        String json = "{\"id\":\"doc-001\",\"domain\":\"test.com\",\"title\":\"Test\"," +
                "\"content\":\"Content\",\"unknownField\":\"value\",\"anotherUnknown\":123}";

        // Act & Assert
        assertDoesNotThrow(() -> {
            DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);
            assertEquals("doc-001", result.getId());
            assertEquals("test.com", result.getDomain());
        });
    }

    @Test
    @DisplayName("JSON Deserialization - Empty JSON")
    void testJsonDeserialization_EmptyJson() throws Exception {
        // Arrange
        String json = "{}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertNull(result.getId());
        assertNull(result.getDomain());
        assertNull(result.getTitle());
        assertNull(result.getContent());
        assertNull(result.getTopicAssignment());
    }

    @Test
    @DisplayName("JSON Deserialization - Null Values")
    void testJsonDeserialization_NullValues() throws Exception {
        // Arrange
        String json = "{\"id\":\"doc-001\",\"domain\":null,\"title\":null," +
                "\"content\":null,\"topicAssignment\":null}";

        // Act
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals("doc-001", result.getId());
        assertNull(result.getDomain());
        assertNull(result.getTitle());
        assertNull(result.getContent());
        assertNull(result.getTopicAssignment());
    }

    // ==================== Tests for Round-Trip Serialization ====================

    @Test
    @DisplayName("Round-Trip Serialization - Core Fields")
    void testRoundTripSerialization_CoreFields() throws Exception {
        // Arrange
        documentRaw.setId("doc-001");
        documentRaw.setDomain("test.com");
        documentRaw.setTitle("Test Title");
        documentRaw.setContent("Test Content");
        documentRaw.setDate("2024-01-15");

        // Act - Serialize then deserialize
        String json = objectMapper.writeValueAsString(documentRaw);
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals(documentRaw.getId(), result.getId());
        assertEquals(documentRaw.getDomain(), result.getDomain());
        assertEquals(documentRaw.getTitle(), result.getTitle());
        assertEquals(documentRaw.getContent(), result.getContent());
        assertEquals(documentRaw.getDate(), result.getDate());
    }

    @Test
    @DisplayName("Round-Trip Serialization - With Topic Assignment")
    void testRoundTripSerialization_WithTopicAssignment() throws Exception {
        // Arrange
        documentRaw.setId("doc-001");
        Map<Integer, Double> topicAssignment = new HashMap<>();
        topicAssignment.put(0, 0.35);
        topicAssignment.put(2, 0.45);
        topicAssignment.put(5, 0.20);
        documentRaw.setTopicAssignment(topicAssignment);

        // Act
        String json = objectMapper.writeValueAsString(documentRaw);
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals(3, result.getTopicAssignment().size());
        assertEquals(0.35, result.getTopicAssignment().get(0), 0.001);
        assertEquals(0.45, result.getTopicAssignment().get(2), 0.001);
        assertEquals(0.20, result.getTopicAssignment().get(5), 0.001);
    }

    @Test
    @DisplayName("Round-Trip Serialization - Complete ParlaMint Document")
    void testRoundTripSerialization_CompleteParlaMint() throws Exception {
        // Arrange - Create complete ParlaMint document
        documentRaw.setId("speech-001");
        documentRaw.setDomain("clarin.si");
        documentRaw.setTitle("Parliamentary Speech");
        documentRaw.setContent("Speech content");
        documentRaw.setDate("2024-01-15");
        documentRaw.setSpeaker_name("John Doe");
        documentRaw.setSpeaker_party("Labour");
        documentRaw.setSpeaker_gender("M");
        documentRaw.setParty_status("Coalition");
        documentRaw.setBody("Commons");

        Map<Integer, Double> topics = new HashMap<>();
        topics.put(0, 0.5);
        topics.put(3, 0.3);
        documentRaw.setTopicAssignment(topics);

        // Act
        String json = objectMapper.writeValueAsString(documentRaw);
        DocumentRaw result = objectMapper.readValue(json, DocumentRaw.class);

        // Assert
        assertEquals(documentRaw.getId(), result.getId());
        assertEquals(documentRaw.getDomain(), result.getDomain());
        assertEquals(documentRaw.getTitle(), result.getTitle());
        assertEquals(documentRaw.getContent(), result.getContent());
        assertEquals(documentRaw.getSpeaker_name(), result.getSpeaker_name());
        assertEquals(documentRaw.getSpeaker_party(), result.getSpeaker_party());
        assertEquals(documentRaw.getSpeaker_gender(), result.getSpeaker_gender());
        assertEquals(documentRaw.getParty_status(), result.getParty_status());
        assertEquals(documentRaw.getBody(), result.getBody());
        assertEquals(2, result.getTopicAssignment().size());
    }

    // ==================== Tests for Lombok Generated Methods ====================

    @Test
    @DisplayName("Lombok @Data - toString() contains fields")
    void testLombokToString() {
        // Arrange
        documentRaw.setId("doc-001");
        documentRaw.setTitle("Test Title");
        documentRaw.setContent("Test Content");

        // Act
        String toString = documentRaw.toString();

        // Assert
        assertTrue(toString.contains("doc-001"));
        assertTrue(toString.contains("Test Title"));
        assertTrue(toString.contains("Test Content"));
    }

    @Test
    @DisplayName("Lombok @Data - equals() and hashCode()")
    void testLombokEqualsAndHashCode() {
        // Arrange
        DocumentRaw doc1 = new DocumentRaw();
        doc1.setId("doc-001");
        doc1.setTitle("Title");

        DocumentRaw doc2 = new DocumentRaw();
        doc2.setId("doc-001");
        doc2.setTitle("Title");

        DocumentRaw doc3 = new DocumentRaw();
        doc3.setId("doc-002");
        doc3.setTitle("Different Title");

        // Act & Assert
        assertEquals(doc1, doc2, "Documents with same values should be equal");
        assertNotEquals(doc1, doc3, "Documents with different values should not be equal");
        assertEquals(doc1.hashCode(), doc2.hashCode(), "Equal documents should have same hashCode");
    }
}