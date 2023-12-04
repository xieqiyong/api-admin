package com.hz.api.admin.web.service.Impl;

import com.alibaba.fastjson.JSON;
import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.vo.RpsVO;
import com.hz.api.admin.stream.data.enums.EsIndexEnum;
import com.hz.api.admin.web.service.DataCenterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataCenterServiceImpl implements DataCenterService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public ResultInfo getRps(long stressId, long startTime, long endTime) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("ts").gte(startTime).lte(endTime));
        List<RpsVO> result = this.search(EsIndexEnum.RPS_SUCCESS.getIndex(),
                SearchSourceBuilder.searchSource().size(1000).query(builder)
                        .sort("ts", SortOrder.ASC), RpsVO.class
        );
        Set<String> keySet = new HashSet<>();
        HashSet<String> time = new HashSet<>();
        Map<String, List<Long>> retMap = new HashMap<>();
        Map<String, List<Long>> retErrMap = new HashMap<>();
        for (RpsVO rpsVO : result) {
            LocalDateTime localDateTime = LocalDateTime
                    .ofEpochSecond(rpsVO.getTs()/1000, 0, ZoneOffset.of("+8"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String key = rpsVO.getKey().substring(0,rpsVO.getKey().length() -28);
            rpsVO.setKey(key);
            keySet.add(key);
            List<Long> counts = new ArrayList<>();
            counts.add(rpsVO.getCount());
            if(retMap.containsKey(key)){
                retMap.get(key).add(rpsVO.getCount());
            }else{
                retMap.put(key, counts);
            }
            if(!time.contains(rpsVO.getTs())){
                time.add(localDateTime.format(dateTimeFormatter));
            }
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("data", retMap);
        ret.put("errData", retErrMap);
        ret.put("key", keySet);
        ret.put("time", time.stream().sorted().collect(Collectors.toList()));
        return ResultInfo.success(ret);
    }

    @Override
    public ResultInfo getErrRps(Long stressId, long startTime, long endTime) {
        return null;
    }

    public <T> List<T> search(String indexName, SearchSourceBuilder searchSourceBuilder, Class<T> clazz) {
        try {
            SearchResponse response = client.search(this.request(indexName, searchSourceBuilder), RequestOptions.DEFAULT);
            List<T> list = new ArrayList<>();
            response.getHits().forEach(var -> list.add(JSON.parseObject(var.getSourceAsString(), clazz)));
            return list;
        } catch (Exception ex) {
            log.error("elasticsearch查询时异常：", ex);
            throw new RuntimeException("数据服务异常，请联系管理员！");
        }
    }

    public SearchRequest request(String indexName, SearchSourceBuilder searchSourceBuilder) {
        log.debug("输出elasticsearch查询语句，index：{}，sql：{}", indexName, searchSourceBuilder.toString());
        SearchRequest request = new SearchRequest(indexName);
        request.source(searchSourceBuilder);

        return request;
    }
}
