package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Class mapped to the Elasticsearch index "project_index"
 *
 * It defines specific field types to leverage full-text search for content and matching for metadata
 */
@Data
@Document(indexName = "project_index")
public class DocumentSearch {
    @Id
    private String id;

    /**
     * The document title
     * Mapped as "FieldType.Text" to allow full-text search in the title
     */
    @Field(type = FieldType.Text)
    private String title;

    /**
     * The body content of the document
     * Mapped as "FieldType.Text" for standard search query matching
     */
    @Field(type = FieldType.Text)
    private String content;

    /**
     * The source URL
     * Mapped as "FieldType.Keyword" to display all documents given a certain URL
     */
    @Field(type = FieldType.Keyword)
    private String url;

    /**
     * The domain of origin
     * Mapped as "FieldType.Keyword" to facilitate the filtering by source
     */
    @Field(type = FieldType.Keyword)
    private String domain;

    /**
     * A nested structure containing the associated topics and their weights
     */
    @Field(type = FieldType.Nested)
    private List<SearchableTopic> topics;

    /**
     * Inner DTO representing a specific topic association
     */
    @Data
    public static class SearchableTopic {
        @Field(type = FieldType.Integer)
        private int topicId;

        @Field(type = FieldType.Double)
        private double weight;
    }
}