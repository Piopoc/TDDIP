package com.example.demo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TextCleaner Utility Tests")
class TextCleanerTest {

    private Set<String> stopWords;

    @BeforeEach
    void setUp() {
        // list of common English stopwords for the tests
        stopWords = Set.of("the", "is", "of", "and", "a", "to", "in");
    }

    //Tests for extractDomainStatic

    @Test
    @DisplayName("extractDomain - Standard URL with WWW")
    void testExtractDomain_StandardUrl() {
        String url = "https://www.repubblica.it/cronaca/news";
        String result = TextCleaner.extractDomainStatic(url);

        assertNotNull(result);
        assertEquals("repubblica.it", result);
    }

    @Test
    @DisplayName("extractDomain - URL without WWW")
    void testExtractDomain_NoWww() {
        String url = "https://example.com/page";
        String result = TextCleaner.extractDomainStatic(url);

        assertEquals("example.com", result);
    }

    @Test
    @DisplayName("extractDomain - URL without Protocol (http/https)")
    void testExtractDomain_NoProtocol() {
        // Without protocol, URI parsing usually fails or returns partial data
        String url = "example.com/page";
        String result = TextCleaner.extractDomainStatic(url);

        assertEquals("unknown", result);
    }

    @Test
    @DisplayName("extractDomain - Invalid URL")
    void testExtractDomain_InvalidUrl() {
        String url = "invalid-fake-url";
        String result = TextCleaner.extractDomainStatic(url);

        assertEquals("unknown", result);
    }

    @Test
    @DisplayName("extractDomain - Null or Empty Input")
    void testExtractDomain_NullOrEmpty() {
        assertEquals("unknown", TextCleaner.extractDomainStatic(null));
        assertEquals("unknown", TextCleaner.extractDomainStatic(""));
    }

    // Tests for removeStopwordsStatic

    @Test
    @DisplayName("removeStopwords - Basic Removal")
    void testRemoveStopwords_Basic() {
        String input = "Pippo is a dog";
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);
        assertEquals("Pippo dog", result);
    }

    @Test
    @DisplayName("removeStopwords - Removes Numbers")
    void testRemoveStopwords_RemovesNumbers() {
        String input = "I bought 3 apples and 100 pears";
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);
        assertEquals("i bought apples pears", result);
    }

    @Test
    @DisplayName("removeStopwords - Case Insensitive Stopwords")
    void testRemoveStopwords_CaseInsensitive() {
        String input = "THE CAT IS OF MARIO";
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);
        assertEquals("cat mario", result);
    }

    @Test
    @DisplayName("removeStopwords - Handles Extra Whitespaces")
    void testRemoveStopwords_ExtraWhitespaces() {
        String input = "Word1    Word2   Word3";
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);
        assertEquals("word word word", result);
    }

    @Test
    @DisplayName("removeStopwords - Null or Empty Input")
    void testRemoveStopwords_NullOrEmpty() {
        // Act & Assert
        assertNull(TextCleaner.removeStopwordsStatic(null, stopWords));
        assertEquals("", TextCleaner.removeStopwordsStatic("", stopWords));
    }
}