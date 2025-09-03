package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.dto.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")
public class DocumentTest {

    private RestHighLevelClient client;

    @Autowired
    private IItemService itemService;

    @Test
    void testDocumentCreate() throws IOException {
        Item item = itemService.getById(317578L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocumentUpdate() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "317578");
        request.doc(
                "price",29900
        );
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocumentGet() throws IOException {
        GetRequest request = new GetRequest("items", "317578");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println("doc = " + doc);
    }

    @Test
    void testDocumentDelete() throws IOException {
        DeleteRequest request = new DeleteRequest("items", "317578");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocumentBulk() throws IOException {

        int pageNo = 1, pageSize = 500;
        while (true){
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> items = page.getRecords();
            if (items == null || items.isEmpty()) {
                return;
            }
            BulkRequest request = new BulkRequest("items");
            for (Item item : items) {
                request.add(new IndexRequest()
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item,ItemDoc.class)), XContentType.JSON));
            }
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
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
