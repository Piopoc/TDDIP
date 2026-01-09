package com.example.demo.util;

import org.springframework.stereotype.Component;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * This class provides methods to normalize raw text by removing numerical values,
 * redundant whitespaces and stopwords
 */
@Component
public class TextCleaner {

    private String extractDomain(String urlString) {
        return extractDomainStatic(urlString);
    }

    private String removeStopwords(String text, Set<String> stopwords) {
        return removeStopwordsStatic(text, stopwords);
    }

    // Regular expression for noise reduction and tokenization
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\w+");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s{2,}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /**
     * Loads and normalizes a list of stopwords from a local resource:
     * each token is trimmed and converted to lowercase
     */
    public static Set<String> loadStopwords(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Orchestrates the cleaning of a JSONL dataset:
     * updates the 'text' field by stripping stopwords and adds the field 'doamin' (extracted from URL)
     */
    public static void processJsonl(String inputPath, String outputPath, Set<String> stopwords) throws IOException {
        Path outputFilePath = Paths.get(outputPath);
        Files.createDirectories(outputFilePath.getParent());

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputPath));
             BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
            String line;
            int count = 0;
            int skipped = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    skipped++;
                    continue;
                }
                try {
                    JSONObject json = new JSONObject(line);
                    if (json.has("text")) {
                        String originalText = json.getString("text");
                        String cleanedText = removeStopwordsStatic(originalText, stopwords);
                        json.put("text", cleanedText);
                    }

                    if (json.has("url")) {
                        String url = json.getString("url");
                        String domain = extractDomainStatic(url);
                        json.put("domain", domain);
                        json.remove("url");
                    }

                    writer.write(json.toString());
                    writer.newLine();
                    count++;
                    if (count % 5000 == 0) {
                        System.out.printf("Elaborati %d articoli...%n", count);
                    }
                } catch (Exception e) {
                    System.err.printf("Errore elaborando riga %d: %s%n", count + skipped + 1, e.getMessage());
                    skipped++;
                }
            }
            System.out.printf("%nElaborazione completata:%n");
            System.out.printf("- Articoli processati: %d%n", count);
            System.out.printf("- Righe saltate/errori: %d%n", skipped);
        }
    }

    /**
     * Stopword and number elimination (static version).
     */
    public static String removeStopwordsStatic(String text, Set<String> stopwords) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        text = NUMBER_PATTERN.matcher(text).replaceAll(" ");

        Matcher matcher = TOKEN_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        while (matcher.find()) {
            String token = matcher.group().toLowerCase();
            if (!stopwords.contains(token)) {
                result.append(token).append(' ');
            }
        }
        return MULTI_SPACE_PATTERN.matcher(result.toString()).replaceAll(" ").trim();
    }

    /**
     * Extracts the domain from a URL (static version).
     */
    public static String extractDomainStatic(String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return "unknown";
        }
        try {
            URI uri = new URI(urlString);
            String domain = uri.getHost();

            if (domain == null) return "unknown";

            return domain.startsWith("www.") ? domain.substring(4) : domain;
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    public static void main(String[] args) {
        String stopwordsFile = "dataset/data/stoplist_en.txt";

        try {
            Set<String> stopwords = loadStopwords(stopwordsFile);

            // NewsArticles
            System.out.println("Cleaning News Articles...");
            processJsonl("dataset/data/mediabias_newsarticles.jsonl", "dataset/output/cleaned_articles.jsonl", stopwords);

            // ParlaMint
            System.out.println("Cleaning ParlaMint...");
            processJsonl("dataset/data/parlamint_raw.jsonl", "dataset/output/cleaned_parlamint.jsonl", stopwords);

            System.out.println("\nCleaning completato!");

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}