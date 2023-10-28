package ru.msu.cs.nosql.nosqlapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;

@SpringBootApplication(
		exclude = { ElasticsearchClientAutoConfiguration.class }
)
public class NosqlappApplication {

	public static void main(String[] args) {
		SpringApplication.run(NosqlappApplication.class, args);
	}

}
