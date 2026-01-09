package com.example.demo.service;

import com.example.demo.model.DocumentRaw;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.util.TextCleaner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
    private final TextCleaner textCleaner;

    /**
     * Orchestrates the process of acquiring data from the local JSONL file to the database:
     * parses the JSONL dataset, deserializes each line into a "DocumentRaw" object,
     * and performs a batch save to MongoDB
     *
     * @throws Exception if file access or parsing fails
     */
    public void importDocumentsFromPipeline() throws Exception {

        File file = new File("dataset/output/enhanced_articles.jsonl");

        if (!file.exists()) {
            throw new RuntimeException("File non trovato in: " + file.getAbsolutePath());
        }

        // Reading and Deserialization phase
        ObjectMapper mapper = new ObjectMapper();
        List<DocumentRaw> documentsToSave = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    // Transform JSON string into a Java Entity
                    DocumentRaw doc = mapper.readValue(line, DocumentRaw.class);

                    // Extracting the domain from the URL
                    if (doc.getUrl() != null) {
                        String domain = textCleaner.extractDomain(doc.getUrl());
                        doc.setDomain(domain);
                    }
                    documentsToSave.add(doc);
                } catch (Exception e) {
                    System.err.println("errore lettura riga JSON: " + e.getMessage());
                }
            }
        }

        // saving to the database
        if (!documentsToSave.isEmpty()) {
            mongoRepository.deleteAll();
            mongoRepository.saveAll(documentsToSave);
            System.out.println("Importazione completata! Salvati " + documentsToSave.size() + " documenti su MongoDB.");
        } else {
            System.out.println("file vuoto o non contente documenti validi");
        }
    }
}