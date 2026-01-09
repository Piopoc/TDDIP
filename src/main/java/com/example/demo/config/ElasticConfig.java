package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value; // <--- Importante
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

/**
 * This class extends "ElasticsearchConfiguration" to provide specialized
 * infrastructure setup for the Elasticsearch connection
 */
@Configuration
public class ElasticConfig extends ElasticsearchConfiguration {
    /**
     * The URI of the Elasticsearch node, injected from the application properties.
     * Defaults to "elasticsearch:9200" to ensure connectivity in containerized
     * environments; if no explicit configuration is provided.
     */
    @Value("${spring.elasticsearch.uris:elasticsearch:9200}")
    private String elasticUri;

    /**
     * Defines the client configuration used to connect to the Elasticsearch cluster.
     * * @return a "ClientConfiguration" instance configured with the processed URI.
     */
    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        // Sanitize the URI by stripping protocol prefixes to comply with ClientConfiguration requirements
        String cleanUri = elasticUri.replace("http://", "").replace("https://", "");

        return ClientConfiguration.builder()
                .connectedTo(cleanUri)
                .build();
    }
}