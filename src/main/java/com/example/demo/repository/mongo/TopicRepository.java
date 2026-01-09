package com.example.demo.repository.mongo;

import com.example.demo.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MongoDB operations on "Topic" entities
 */
@Repository
public interface TopicRepository extends MongoRepository<Topic, Integer> {
}