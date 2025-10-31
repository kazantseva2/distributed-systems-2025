package ru.msu.cs.nosql.nosqlapp.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.stereotype.Repository;
import ru.msu.cs.nosql.nosqlapp.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ElasticUserRepository {

    public static final String INDEX_NAME = "users";
    private ElasticsearchClient esClient;

    public ElasticUserRepository() {
        String serverUrl = "http://localhost:9200";
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "123456"));
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        esClient = new ElasticsearchClient(transport);
    }

    public User save(User user) {
        try {
            esClient.index(i ->
                    i.index("users")
                            .id(user.getId().toString())
                            .document(user)
            );
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    public List<User> findByName(String name) {
        try {
            var results = esClient.search(sr ->
                    sr.index(INDEX_NAME)
                            .query(q -> q
                                    .match(t -> t
                                            .field("name")
                                            .query(name)
                                    )
                            ),
                    User.class
            );
            return results
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
