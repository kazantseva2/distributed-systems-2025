package ru.msu.cs.nosql.nosqlapp.repository;

import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Repository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import ru.msu.cs.nosql.nosqlapp.model.Review;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ElasticReviewRepository {

    public static final String INDEX_NAME = "reviews";
    private final ElasticsearchClient esClient;

    public ElasticReviewRepository() {
        RestClient restClient = RestClient
                .builder(HttpHost.create("http://localhost:9200"))
                .build();

        ElasticsearchTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());

        this.esClient = new ElasticsearchClient(transport);
    }

    public void save(Review review) {
        try {
            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(review.getId())
                    .document(review)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Review> searchByText(String text) {
        try {
            var response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .match(m -> m
                                            .field("text")
                                            .query(text)
                                    )
                            ),
                    Review.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteById(String reviewId) {
        try {
            esClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(reviewId)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete review from Elasticsearch", e);
        }
    }
}