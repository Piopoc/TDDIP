package com.example.demo.util;
import cc.mallet.types.Instance;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * This class implements the  "Iterator" interface to transform tab-separated
 * values into Mallet "Instance" objects
 */
public class DocumentIterator implements Iterator<Instance> {
    private Scanner scanner;

    public DocumentIterator(InputStream dataInputStream) {
        scanner = new Scanner(dataInputStream);
    }

    /**
     * Decodes each line of the input stream into a Mallet Instance:
     * splits the record using the tab delimiter to extract the
     * unique identifier, the metadata label, and the core textual content
     */
    public Instance next() {
        String line = scanner.nextLine();
        String[] lineEntries = line.split("\t");

        String docId = lineEntries[0];  // identifier of the document
        String label = lineEntries[1];  // label of the document
        String text = lineEntries[2];   // content (text) of the document
        return new Instance(text, label, docId, "");
    }
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public void remove() {
        throw new IllegalStateException("This Iterator<Instance> does not support remove().");
    }
}