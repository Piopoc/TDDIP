package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import com.example.demo.service.TopicModelingService;
import com.example.demo.service.FetchService;

@SpringBootTest
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration"
})
class DemoApplicationTests {
    @Autowired
    private TopicModelingService topicModelingService;
    @Autowired
    private FetchService fetchService;
	@Test
	void testFullPipeline() {
        try{
            System.out.println(">>>> Inzio test FetchService ...");
            fetchService.importDocumentsFromPipeline();
            System.out.println(">>>> Inizio test TopicModellingService ...");
            topicModelingService.runAnalysis();
            System.out.println(">>>> Test completato! Controlla Mongo Express.");
        }catch (Exception e){
            System.err.println(">>>> Errore durante il test: "+e.getMessage());
            e.printStackTrace();
        }
	}
}
