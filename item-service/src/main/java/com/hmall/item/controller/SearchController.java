package com.hmall.item.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final IItemService itemService;
    private final RestHighLevelClient client;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) throws IOException {
     /*   // 分页查询
        Page<Item> result = itemService.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .eq(Item::getStatus, 1)
                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
                .page(query.toMpPage("update_time", false));
        // 封装并返回
        return PageDTO.of(result, ItemDTO.class);*/
        PageDTO<ItemDTO> pageDTO = new PageDTO<>();
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StrUtil.isNotBlank(query.getKey())){
            boolQueryBuilder.must().add(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        boolQueryBuilder.filter(QueryBuilders.termQuery("status", 1));
        if (query.getMaxPrice() != null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }
        if (query.getMinPrice() != null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        request.source().size(0)
                .query(boolQueryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String item = hit.getSourceAsString();
            ItemDTO itemDTOe = JSONUtil.toBean(item, ItemDTO.class);
            pageDTO.getList().add(itemDTOe);
        }
        for (ItemDTO itemDTO : pageDTO.getList()) {
            System.out.println(itemDTO.toString());
        }
        return pageDTO;
    }
}
