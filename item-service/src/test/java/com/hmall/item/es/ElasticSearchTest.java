package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.TermVectorsResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.similarity.ScriptedSimilarity;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ElasticSearchTest {

    private RestHighLevelClient client;


    @Test
    void testMatchAllSearch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        System.out.println("Total Hits: " + total);
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String json = hit.getSourceAsString();
            System.out.println(json);
        }
    }

    @Test
    void testLeafSearch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.rangeQuery("price").from("10000").to("50000"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.err.println(json);
        }
    }

    @Test
    void testSearch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must().add(QueryBuilders.matchQuery("name","脱脂牛奶"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lt(10000));
        boolQueryBuilder.filter(QueryBuilders.termQuery("brand","德亚"));
        request.source()
                .query(boolQueryBuilder)
                .from(0)
                .size(5)
                .sort("sold", SortOrder.DESC)
                .sort("price", SortOrder.ASC);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.err.println(json);
        }
    }

    @Test
    void testHighlight() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .highlighter(SearchSourceBuilder.highlight().field("name").preTags("<em>").postTags("</em>"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!highlightFields.isEmpty()){
                HighlightField name = highlightFields.get("name");
                Text[] text = name.getFragments();
                String highlight = text[0].string();
                System.err.println(highlight);
            }
        }

    }


    @Test
    void testAgg() throws IOException {
        String aggName = "brandAgg";
        SearchRequest request = new SearchRequest("items");
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms(aggName).field("brand").size(10));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms aggregation = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("key: " + key);
            System.out.println("docCount: " + docCount);
        }
    }



    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("10.41.5.44:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
