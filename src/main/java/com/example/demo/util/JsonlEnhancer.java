package com.example.demo.util;

import org.json.JSONObject;
import java.io.*;
import java.util.*;

/**
 * This class ensures that the ingested documents follow a specific field ordering
 * and filters out redundant metadata before the persistence phase
 */
public class JsonlEnhancer {
    private static final Set<String> COLUMNS_TO_REMOVE = Set.of(
            "sources"
    );
    private static final List<String> COLUMN_ORDER = Arrays.asList(
            "id", "url", "domain", "title", "text", "date", "documentLength"
    );
    public static void main(String[] args) {
        String inputFile = "dataset/output/cleaned_articles.jsonl";
        String outputFile = "dataset/output/enhanced_articles.jsonl";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                JSONObject original = new JSONObject(line);
                String orderedJson = buildOrderedJson(original);
                writer.write(orderedJson);
                writer.newLine();
            }
            System.out.println("Elaborazione completata: " + outputFile);
        } catch (IOException e) {
            System.err.println("Errore nella lettura/scrittura del file: " + e.getMessage());
        }
    }

    /**
     * Manually constructs a specific ordered JSON string
     */
    private static String buildOrderedJson(JSONObject obj) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (String key : COLUMN_ORDER) {
            if (obj.has(key) && !COLUMNS_TO_REMOVE.contains(key)) {
                if (!first) json.append(",");
                appendKeyValue(json, key, obj.get(key));
                first = false;
            }
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Performs low-level JSON serialization for key-value pairs
     */
    private static void appendKeyValue(StringBuilder json, String key, Object value) {
        json.append("\"").append(escapeJson(key)).append("\":");
        if (value == null || value == JSONObject.NULL) {
            json.append("null");
        } else if (value instanceof String) {
            json.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else {
            json.append(value.toString());
        }
    }

    /**
     * Sanitizes string literals to ensure JSON compatibility
     */
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}