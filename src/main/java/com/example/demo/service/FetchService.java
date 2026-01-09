package com.example.demo.service;

import com.example.demo.model.DocumentRaw;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.util.TextCleaner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for the initial data ingestion phase
 *
 * It handles the extraction of raw data from JSONL files, performs initial
 * cleaning and domain extraction, and persists the resulting entities into MongoDB
 *
 */
@Service
@RequiredArgsConstructor
public class FetchService {

    private final MongoDocumentRepository mongoRepository;

    /**
     * Orchestrates the process of acquiring data from the local JSONL file to the database:
     * parses the JSONL dataset, deserializes each line into a "DocumentRaw" object,
     * and performs a batch save to MongoDB
     *
     * @throws Exception if file access or parsing fails
     */
    public void importDocumentsFromPipeline() throws Exception {

        List<String> files = Arrays.asList(
                "dataset/output/enhanced_articles.jsonl",
                "dataset/output/enhanced_parlamint.jsonl"
        );

        ObjectMapper mapper = new ObjectMapper();

        // ignore unknown fields if we don't use @JsonAnySetter
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<DocumentRaw> allDocs = new ArrayList<>();

        for (String filePath : files) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Salto file non trovato: " + filePath);
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        DocumentRaw doc = mapper.readValue(line, DocumentRaw.class);
                        allDocs.add(doc);
                    } catch (Exception e) {
                        System.err.println("Errore in " + filePath + ": " + e.getMessage());
                    }
                }
            }
        }

        // saving to the database
        if (!allDocs.isEmpty()) {
            mongoRepository.deleteAll();
            mongoRepository.saveAll(allDocs);
            System.out.println("Importazione completata! Salvati " + allDocs.size() + " documenti su MongoDB.");
        }
    }
}