package com.example.demo.service;

import com.example.demo.model.DocumentRaw;
import com.example.demo.model.DocumentSearch;
import com.example.demo.repository.elastic.ElasticDocumentRepository;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service dedicated to data synchronization between MongoDB and Elasticsearch
 */
@Service
@RequiredArgsConstructor
public class IndexService {

    private final MongoDocumentRepository mongoDocumentRepository;
    private final ElasticDocumentRepository elasticDocumentRepository;

    /**
     * Synchronizes the document corpus to the Elasticsearch index
     */
    public void syncMongoElastic() {
        System.out.println("--- Inizio sincronizzazione da Mongo ad Elastic ---");

        List<DocumentRaw> rawDocs = mongoDocumentRepository.findAll();

        if (rawDocs.isEmpty()) {
            System.out.println("ATTENZIONE: nessun documento trovato in Mongo");
            return;
        }
        System.out.println("Trovati " + rawDocs.size() + " documenti in Mongo. Inizio conversione...");

        // Stream-based conversion: DocumentRaw -> DocumentSearch
        List<DocumentSearch> searchDocs = rawDocs.stream().map(this::mapRawToSearch).collect(Collectors.toList());

        elasticDocumentRepository.saveAll(searchDocs);

        System.out.println("--- Sincronizzazione completata! ---");
    }

    /**
     * Helper method for Data Transformation:
     * maps fields from the MongoDB entity to the Elasticsearch object
     */
    private DocumentSearch mapRawToSearch(DocumentRaw raw) {
        DocumentSearch search = new DocumentSearch();

        search.setId(raw.getId());
        search.setTitle(raw.getTitle());
        search.setDomain(raw.getDomain());
        search.setContent(raw.getContent());

        // Mapping topics: from Map<Integer, Double> to List<SearchableTopic>
        if (raw.getTopicAssignment() != null) {
            List<DocumentSearch.SearchableTopic> topicList = new ArrayList<>();
            for (Map.Entry<Integer, Double> entry : raw.getTopicAssignment().entrySet()) {
                DocumentSearch.SearchableTopic t = new DocumentSearch.SearchableTopic();
                t.setTopicId(entry.getKey());
                t.setWeight(entry.getValue());
                topicList.add(t);
            }
            search.setTopics(topicList);
        }
        return search;
    }
}