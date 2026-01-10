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

    private String speaker_name;
    private String speaker_party;
    private String speaker_party_name;
    private String speaker_gender;
    private String speaker_role;
    private String speaker_id;
    private String speaker_mp;
    private String speaker_birth;
    private String speaker_minister;
    private String party_status;
    private String party_orientation;
    private String subcorpus;
    private String session;
    private String body;
    private String agenda;
    private String topic;
    private String meeting;
    private String sitting;
    private String term;
    private String lang;
}