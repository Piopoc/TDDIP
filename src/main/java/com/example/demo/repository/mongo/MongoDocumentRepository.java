package com.example.demo.repository.mongo;

import com.example.demo.model.DocumentRaw;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MongoDB operations on "DocumentRaw" entities
 */
@Repository
public interface MongoDocumentRepository extends MongoRepository<DocumentRaw, String> {
}