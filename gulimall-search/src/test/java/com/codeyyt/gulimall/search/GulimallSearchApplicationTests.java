package com.codeyyt.gulimall.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient elasticClient;

    @Test
    public void testElasticClient() {
        SearchRequest searchRequest = new SearchRequest();
    }

    @Test
    public void contextLoads() {
        System.out.println(elasticClient);
    }
}
