package com.example.demo.repository.elastic;

import com.example.demo.model.DocumentSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Elasticsearch operations on "DocumentSearch" entities
 *
 * This component supports:
 * - Full-Text Search: partial match on the document content
 * - Field Search: partial match based on a field other than the content field according to the syntax “field:value”
 */
@Repository
public interface ElasticDocumentRepository extends ElasticsearchRepository<DocumentSearch, String> {

    /**
     * Executes a full-text search within the 'content' field
     */
    List<DocumentSearch> findByContentContaining(String content);

    /**
     * Filters documents by matching a substring within the 'domain' metadata field
     */
    List<DocumentSearch> findByDomainContaining(String domain);

    /**
     * Filters documents by matching a substring within the 'title' field
     */
    List<DocumentSearch> findByTitleContaining(String title);

    /**
     * Filters documents by matching a substring within the 'url' field
     */
    List<DocumentSearch> findByUrlContaining(String url);
}