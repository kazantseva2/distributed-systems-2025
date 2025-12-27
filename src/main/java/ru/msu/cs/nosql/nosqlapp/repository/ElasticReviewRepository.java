package ru.msu.cs.nosql.nosqlapp.repository;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.JsonData;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.stereotype.Repository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import ru.msu.cs.nosql.nosqlapp.model.RatingOperator;
import ru.msu.cs.nosql.nosqlapp.model.Review;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ElasticReviewRepository {

    public static final String INDEX_NAME = "reviews";
    private final ElasticsearchClient esClient;

    public void createIndexIfNotExists() {
        try {
            boolean exists = esClient.indices().exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                CreateIndexResponse response = esClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m
                                .properties("id", p -> p.keyword(k -> k))
                                .properties("productId", p -> p.keyword(k -> k))
                                .properties("text", p -> p.text(t -> t
                                        .fields("keyword", f -> f.keyword(k -> k))
                                        .analyzer("standard") // добавляем стандартный анализатор
                                ))
                                .properties("rating", p -> p.integer(i -> i))
                                .properties("date", p -> p.date(d -> d))
                        )
                );
                System.out.println("Created index: " + response.index());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index", e);
        }
    }


    public ElasticReviewRepository() throws Exception {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "123456"));

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (certificate, authType) -> true)
                .build();

        RestClient restClient = RestClient.builder(HttpHost.create("https://localhost:9200"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.esClient = new ElasticsearchClient(transport);
        createIndexIfNotExists();
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

    public List<Review> searchByProductAndRatingAndText(String productId, Integer rating,
                                                        RatingOperator ratingOperator, String text) {
        try {
            var response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q.bool(b -> {
                                if (productId != null) {
                                    b.must(m -> m.term(t -> t.field("productId").value(productId)));
                                }

                                if (rating != null && ratingOperator != null) {
                                    b.must(m -> m.range(r -> {
                                        r.field("rating");
                                        return switch (ratingOperator) {
                                            case EQ -> r.gte(JsonData.of(rating)).lte(JsonData.of(rating));
                                            case GTE -> r.gte(JsonData.of(rating));
                                            case LTE -> r.lte(JsonData.of(rating));
                                        };
                                    }));
                                }

                                if (text != null && !text.isEmpty()) {
                                    b.must(m -> m.match(mt -> mt.field("text").query(text)));
                                }

                                return b;
                            })),
                    Review.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search failed", e);
        }
    }

    public Map<String, Double> getRatingTrendByProduct(String productId) {
        try {
            var response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .size(0)
                            .query(q -> q.term(t -> t.field("productId").value(productId)))
                            .aggregations("ratings_over_time", a -> a
                                    .dateHistogram(dh -> dh
                                            .field("date")
                                            .calendarInterval(co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval.Month)
                                    )
                                    .aggregations("avg_rating", aa -> aa
                                            .avg(avg -> avg.field("rating"))
                                    )
                            ),
                    Void.class
            );

            var buckets = response.aggregations()
                    .get("ratings_over_time")
                    .dateHistogram()
                    .buckets()
                    .array();

            Map<String, Double> result = new HashMap<>();
            for (var bucket : buckets) {
                double avg = bucket.aggregations()
                        .get("avg_rating")
                        .avg()
                        .value();
                result.put(bucket.keyAsString(), avg);
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to get rating analytics", e);
        }
    }

    public Map<String, Long> getCommonWordsInNegativeReviews() {
        try {
            // 1. Сначала получаем все негативные отзывы
            var response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .size(1000) // Ограничиваем для производительности
                            .query(q -> q.range(r -> r.field("rating").lte(JsonData.of(3)))),
                    Review.class
            );

            // 2. Анализируем текст на стороне приложения
            Map<String, Long> wordCount = new HashMap<>();

            // Список стоп-слов для фильтрации
            Set<String> stopWords = new HashSet<>(Arrays.asList(
                    "the", "a", "an", "and", "or", "but", "in", "on", "at", "to",
                    "for", "of", "with", "by", "as", "is", "are", "was", "were",
                    "be", "been", "being", "have", "has", "had", "having", "do",
                    "does", "did", "doing", "this", "that", "these", "those",
                    "it", "its", "it's", "they", "them", "their", "what", "which",
                    "who", "whom", "whose", "when", "where", "why", "how", "not",
                    "no", "yes", "so", "too", "very", "just", "only", "also",
                    "then", "than", "there", "here", "from", "out", "up", "down",
                    "my", "your", "his", "her", "our", "their", "i", "you", "he",
                    "she", "we", "they", "me", "him", "us", "them"
            ));

            // Проходим по всем отзывам
            for (Hit<Review> hit : response.hits().hits()) {
                if (hit.source() != null && hit.source().getText() != null) {
                    String text = hit.source().getText().toLowerCase();

                    // Простая токенизация - разбиваем на слова
                    String[] words = text.split("[\\s\\p{Punct}]+");

                    // Подсчитываем слова
                    for (String word : words) {
                        word = word.trim();
                        // Фильтруем:
                        // 1. Не пустые
                        // 2. Длиннее 2 символов
                        // 3. Не стоп-слова
                        // 4. Только буквы (опционально)
                        if (!word.isEmpty() &&
                                word.length() > 2 &&
                                !stopWords.contains(word) &&
                                word.matches("[a-z]+")) {

                            wordCount.put(word, wordCount.getOrDefault(word, 0L) + 1);
                        }
                    }
                }
            }

            // 3. Возвращаем топ-10 самых частых слов
            return wordCount.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get common words analytics", e);
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

    public void deleteAll() {
        try {
            // Удаляем все документы из индекса
            esClient.deleteByQuery(d -> d
                    .index(INDEX_NAME)
                    .query(q -> q.matchAll(ma -> ma))
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete all reviews from Elasticsearch", e);
        }
    }

}