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
            "sources", "documentLength", "additionalFields"
    );

    private static final List<String> COLUMN_ORDER = Arrays.asList(
            "id", "url", "domain", "title", "text", "date"
    );

    public static void main(String[] args) {
        processFile("dataset/output/cleaned_articles.jsonl", "dataset/output/enhanced_articles.jsonl");
        processFile("dataset/output/cleaned_parlamint.jsonl", "dataset/output/enhanced_parlamint.jsonl");
    }

    public static void processFile(String input, String output) {
        File fileIn = new File(input);
        if(!fileIn.exists()) {
            System.err.println("File non trovato: " + input);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(input));
             BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                JSONObject original = new JSONObject(line);
                String id = original.optString("id", "");

                if (!isNewsId(id)) {
                    if (original.has("additionalFields")) {
                        Object fieldObj = original.get("additionalFields");
                        if (fieldObj instanceof JSONObject) {
                            JSONObject extras = (JSONObject) fieldObj;
                            for (String key : extras.keySet()) {
                                original.put(key, extras.get(key));
                            }
                        }
                    }
                }

                writer.write(buildOrderedJson(original));
                writer.newLine();
            }
            System.out.println("Enhancer completato: " + output);
        } catch (IOException e) {
            System.err.println("Errore in Enhancer: " + e.getMessage());
        }
    }

    private static boolean isNewsId(String id) {
        if (id == null) return false;
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

        for (String key : obj.keySet()) {
            if (!COLUMN_ORDER.contains(key) && !COLUMNS_TO_REMOVE.contains(key)) {
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