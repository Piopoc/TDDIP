package com.example.demo.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing MongoDB "raw_documents" collection
 *
 * Class that stores the main dataset (metadata and results of topic modeling) maintaining
 * a reference to the global topics defined in the "topics" collection
 */
@Data
@Document(collection = "raw_documents")
public class DocumentRaw {
    @Id
    private String id;
    private String domain;
    private String title;

    /**
     * The "JsonAlias" annotation ensures correct mapping from the "text" field
     * found in the source JSONL pipeline files
     */
    @JsonAlias("text")
    private String content;

    private String date;

    /**
     * Maps the Topic ID to its corresponding weight for this document
     */
    private Map<Integer, Double> topicAssignment;

    @JsonIgnore
    private Map<String, Object> additionalFields = new HashMap<>();

    @JsonAnySetter
    public void addAdditionalField(String key, Object value) {
        this.additionalFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }
}