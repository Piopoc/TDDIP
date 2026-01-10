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
        // Il metodo converte tutto in lowercase
        assertEquals("pippo dog", result);
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


    @Test
    @DisplayName("removeStopwords - Null Stopwords Set")
    void testRemoveStopwords_NullStopwordsSet() {
        // Arrange
        String input = "Hello world the cat";

        // Act & Assert
        // The method throws NullPointerException when stopwords.contains() is called on null
        assertThrows(NullPointerException.class, () -> {
            TextCleaner.removeStopwordsStatic(input, null);
        });
    }

    @Test
    @DisplayName("removeStopwords - Empty Stopwords Set")
    void testRemoveStopwords_EmptyStopwordsSet() {
        // Arrange
        String input = "Hello world the cat";
        Set<String> emptyStopWords = Set.of(); // Set vuoto

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, emptyStopWords);

        // Assert
        // Con stopwords vuoto, dovrebbe mantenere tutte le parole (lowercase)
        assertEquals("hello world the cat", result);
    }

// ==================== Test per caratteri speciali ====================

    @Test
    @DisplayName("removeStopwords - Special Characters Punctuation")
    void testRemoveStopwords_SpecialCharactersPunctuation() {
        // Arrange
        String input = "Hello, world! How are you?";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertNotNull(result);
        // Verifica che la punteggiatura sia rimossa
        assertFalse(result.contains(","));
        assertFalse(result.contains("!"));
        assertFalse(result.contains("?"));
        // Verifica che le parole rimangano (in lowercase)
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
        assertTrue(result.contains("how"));
        assertTrue(result.contains("are"));
        assertTrue(result.contains("you"));
    }

    @Test
    @DisplayName("removeStopwords - Special Characters Symbols")
    void testRemoveStopwords_SpecialCharactersSymbols() {
        // Arrange
        String input = "Email: test@example.com #hashtag @mention";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertNotNull(result);
        // Verifica che il metodo non crashi con caratteri speciali
        assertTrue(result.length() > 0);
        // Verifica che estragga le parole valide
        assertTrue(result.contains("email"));
        assertTrue(result.contains("test"));
        assertTrue(result.contains("example"));
        assertTrue(result.contains("com"));
        assertTrue(result.contains("hashtag"));
        assertTrue(result.contains("mention"));
    }

    @Test
    @DisplayName("removeStopwords - Only Special Characters")
    void testRemoveStopwords_OnlySpecialCharacters() {
        // Arrange
        String input = "!@#$%^&*()";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        // Non ci sono token validi, quindi ritorna stringa vuota
        assertNotNull(result);
        assertTrue(result.isEmpty() || result.isBlank());
    }

    @Test
    @DisplayName("removeStopwords - Unicode Accented Characters")
    void testRemoveStopwords_UnicodeAccents() {
        // Arrange
        String input = "café résumé naïve";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertNotNull(result);
        // Verifica che gestisca correttamente gli accenti (in lowercase)
        assertEquals("café résumé naïve", result);
    }

// ==================== Test per edge cases ====================

    @Test
    @DisplayName("removeStopwords - Very Long Text")
    void testRemoveStopwords_VeryLongText() {
        // Arrange
        String input = "word ".repeat(1000) + "test"; // 1000 parole + "test"

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test"));
        // Verifica che non crashi con testi molto lunghi
    }

    @Test
    @DisplayName("removeStopwords - Only Stopwords")
    void testRemoveStopwords_OnlyStopwords() {
        // Arrange
        String input = "the is of and a to in";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        // Se tutte le parole sono stopwords, dovrebbe ritornare stringa vuota
        assertNotNull(result);
        assertTrue(result.isEmpty() || result.isBlank());
    }

    @Test
    @DisplayName("removeStopwords - Single Word Stopword")
    void testRemoveStopwords_SingleWordStopword() {
        // Arrange
        String input = "the";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertTrue(result.isEmpty() || result.isBlank());
    }

    @Test
    @DisplayName("removeStopwords - Single Word Non-Stopword")
    void testRemoveStopwords_SingleWordNonStopword() {
        // Arrange
        String input = "hello";

        // Act
        String result = TextCleaner.removeStopwordsStatic(input, stopWords);

        // Assert
        assertEquals("hello", result);
    }
}