package com.example.demo.controller;

import com.example.demo.repository.elastic.ElasticDocumentRepository;
import com.example.demo.service.FetchService;
import com.example.demo.service.IndexService;
import com.example.demo.service.TopicModelingService;
import com.example.demo.model.DocumentSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller that orchestrates the document processing pipeline
 *
 * This component exposes endpoints for the entire lifecycle of the data:
 * from initial ingestion and topic modeling analysis (with Mallet)
 * to synchronization with the search engine and final information retrieval
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {
    private final FetchService fetchService;
    private final TopicModelingService topicModelingService;
    private final IndexService indexService;
    private final ElasticDocumentRepository elasticDocumentRepository;

    /**
     * Triggers the parsing of raw JSONL files and persists
     * the initial document dataset into the primary MongoDB storage
     *
     * @return a "ResponseEntity" with operation status
     */
    @PostMapping("/import-from-pipeline")
    public ResponseEntity<String> importFromPipeline() {
        try {
            System.out.println("Inizio importazione da file JSONL: ");
            fetchService.importDocumentsFromPipeline();
            return ResponseEntity.ok("Importazione completata");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Errore importazione: " + e.getMessage());
        }
    }

    /**
     * Enriches the existing documents with topic distributions
     * and updates the MongoDB collections to reflect the analytical results
     *
     * @return a "ResponseEntity" with operation status
     */
    // avvio e training Mallet: aggiornamento di MongoDB -> 2 collezioni di documenti
    @PostMapping("/analyze")
    public ResponseEntity<String> runAnalysis() {
        try {
            topicModelingService.runAnalysis();
            System.out.println("Analisi conclusa correttamente. Controlla Mongo Express...");
            return ResponseEntity.ok("Analisi avviata correttamente. Il processo è in esecuzione in background, controlla i log di docker o aggiorna Mongo Express fra circa un minuto...");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore: " + e.getMessage());
        }
    }

    /**
     * Synchronizes the enriched documents from MongoDB to Elasticsearch
     *
     * @return a "ResponseEntity" with operation status
     */
    @PostMapping("/sync-elastic")
    public ResponseEntity<String> syncToElastic() {
        try {
            indexService.syncMongoElastic();
            return ResponseEntity.ok("Sincronizzazione Mongo --> Elastic completata con successo!");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Errore durante la sincronizzazione: " + e.getMessage());
        }
    }

    /**
     * Performs a search query against the Elasticsearch index
     * It supports both field-specific queries (key:value)
     * and general full-text search across document content
     *
     * @param query the search string provided by the client
     * @return a list of "DocumentSearch" objects matching the criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentSearch>> searchDocuments(@RequestParam("query") String query) {

        // Check if the query contains a field separator ":"
        // The check ensures the separator is not at the beginning of the string
        if (query.contains(":") && query.indexOf(":") > 0) {

            // Parse the query into key and value components
            // Limit the split to 2 parts to handle values containing ':'
            String[] parts = query.split(":", 2);

            String prefix = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            System.out.println("Ricerca mirata -> Campo: " + prefix + ", valore: " + value);

            // Route the query to the specific repository method based on the field prefix
            switch (prefix) {
                case "domain":
                    return ResponseEntity.ok(elasticDocumentRepository.findByDomainContaining(value));
                case "title":
                    return ResponseEntity.ok(elasticDocumentRepository.findByTitleContaining(value));
                case "url":
                    return ResponseEntity.ok(elasticDocumentRepository.findByUrlContaining(value));
                case "id":
                    // Handle the Optional return type from findById.
                    return elasticDocumentRepository.findById(value)
                            .map(doc -> ResponseEntity.ok(List.of(doc)))
                            .orElse(ResponseEntity.ok(List.of()));
                default:
                    System.out.println("Prefisso '" + prefix + "' non riconosciuto. Eseguo ricerca full text standard");
            }
        }

        // Standard Full-Text search on content if no valid prefix is detected
        System.out.println("Ricerca Full-Text standard: " + query);
        return ResponseEntity.ok(elasticDocumentRepository.findByContentContaining(query));
    }
}