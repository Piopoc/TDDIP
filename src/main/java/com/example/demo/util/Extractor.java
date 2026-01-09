package com.example.demo.util;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Extractor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void extractDocumentsFromTSVFile(File f, BufferedWriter writer, String country) throws IOException {
        String utterancesPath = f.getAbsolutePath().replace("-meta.tsv", ".txt");
        Map<String, String> utterances = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(utterancesPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) utterances.put(parts[0], parts[1]);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String[] header = br.readLine().split("\t");

            while ((line = br.readLine()) != null) {
                String[] entries = line.split("\t");
                if (entries.length < header.length) continue;

                Map<String, Object> doc = new LinkedHashMap<>();
                Map<String, String> rawData = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    rawData.put(header[i].toLowerCase(), entries[i]);
                }

                String id = rawData.get("id");
                doc.put("id", id);
                doc.put("domain", "clarin.si");
                doc.put("title", rawData.get("title"));
                doc.put("text", utterances.get(id));
                doc.put("date", rawData.get("date"));

                for (String key : rawData.keySet()) {
                    if (!doc.containsKey(key)) {
                        doc.put(key, rawData.get(key));
                    }
                }
                writer.write(MAPPER.writeValueAsString(doc));
                writer.newLine();
            }
        }
    }

    public static void extractAll(String country) throws IOException {
        String inputPath = "./dataset/data/ParlaMint-" + country + "/ParlaMint-" + country + ".txt/";
        File directory = new File(inputPath);

        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Percorso sorgente ParlaMint non trovato o non valido: " + inputPath);
        }
        File[] folders = directory.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            throw new IOException("Nessuna sottocartella (anno) trovata in: " + inputPath);
        }
        File outDir = new File("dataset/data/");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("dataset/data/parlamint_raw.jsonl"))) {
            boolean filesFound = false;
            for (File folder : folders) {
                File[] files = folder.listFiles((dir, name) -> name.endsWith("-meta.tsv"));
                if (files != null && files.length > 0) {
                    filesFound = true;
                    for (File f : files) {
                        extractDocumentsFromTSVFile(f, writer, country);
                    }
                }
            }
            if (!filesFound) {
                throw new IOException("Nessun file -meta.tsv trovato nelle sottocartelle di: " + inputPath);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        extractAll("GB");
    }
}