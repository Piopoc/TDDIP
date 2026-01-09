package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Entity representing MongoDB "topics" collection
 *
 * Class that acts like a metadata repository (top words and weights) for
 * the topics extracted from the document content
 */
@Document(collection = "topics")
@Data
public class Topic {
    @Id
    private Integer topicId;
    private double weight;
    private List<String> topWords;
}