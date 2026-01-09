package com.example.demo.service;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.example.demo.model.DocumentRaw;
import com.example.demo.model.Topic;
import com.example.demo.repository.mongo.MongoDocumentRepository;
import com.example.demo.repository.mongo.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This service performs Topic Modeling (via Mallet) to identify themes within the document corpus;
 * it uses an asynchronous approach to handle the high computational cost of the training phase
 */
@Service
@RequiredArgsConstructor
public class TopicModelingService {
    private final MongoDocumentRepository mongoRepository;
    private final TopicRepository topicRepository;

    /**
     * The process involves:
     * 1. Tokenization and filtering through a Mallet Pipeline
     * 2. Training a Parallel Topic Model
     * 3. Persisting global topic metadata and updating individual document assignments
     */
    @Async
    public void runAnalysis() throws Exception {
        List<DocumentRaw> documents = mongoRepository.findAll();
        if (documents.isEmpty()) return;

        // Mallet Pre-processing Pipeline: converts raw text into feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new CharSequenceLowercase());  // Normalization
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}{3,}")));   // Tokenization
        pipeList.add(new TokenSequence2FeatureSequence());  // Feature extraction

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        for (DocumentRaw doc : documents) {
            if (doc.getContent() != null && !doc.getContent().isBlank()) {
                instances.addThruPipe(new Instance(doc.getContent(), null, doc.getId(), null));
            }
        }

        // Stores the top 15 words representing the semantic core of each topic (10)
        int numTopics = 10;
        ParallelTopicModel model = new ParallelTopicModel(numTopics);
        model.addInstances(instances);
        model.setNumIterations(500);
        model.estimate();

        topicRepository.deleteAll();    // Reset topics from the previous analysis
        List<Topic> globalTopics = new ArrayList<>();
        Object[][] topWords = model.getTopWords(10);

        for(int i = 0; i<numTopics; i++){
            Topic newTopic = new Topic();
            newTopic.setTopicId(i);
            newTopic.setWeight(model.alpha[i]);     // Weight of the topic within the entire document corpus
            List<String> words = new ArrayList<>();
            for(Object obj : topWords[i]){
                words.add((String) obj);
            }
            newTopic.setTopWords(words);
            globalTopics.add(newTopic);
        }
        topicRepository.saveAll(globalTopics);

        // Updates each document with its probability distribution
        // Only assignments exceeding a significance threshold (0.05) are persisted
        for (int i = 0; i < instances.size(); i++) {
            double[] probs = model.getTopicProbabilities(i);
            Map<Integer, Double> assignments = new HashMap<>();
            for (int j = 0; j < numTopics; j++) {
                if(probs[j] > 0.05) {
                    assignments.put(j, probs[j]);
                }
            }
            String docId = (String) instances.get(i).getName();
            mongoRepository.findById(docId).ifPresent(doc -> {
                doc.setTopicAssignment(assignments);
                mongoRepository.save(doc);
            });
        }
    }
}