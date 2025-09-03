package com.hmall.item.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ElasticConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() throws IOException {
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("10.41.5.44:9200")
        ));
    }
}
