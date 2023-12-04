package com.hz.api.admin.stream.write;

import com.alibaba.fastjson.JSON;
import com.hz.api.admin.stream.data.enums.EsIndexEnum;
import com.hz.api.admin.stream.data.model.write.RpsWrite;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RpsErrEs implements WriteEsManager{

    private List<RpsWrite> list = new ArrayList<>();

    @Autowired
    private RestHighLevelClient client;

    @Override
    public String topic() {
        return EsIndexEnum.RPS_FAIL.getIndex();
    }

    @Override
    public void writeEs(String value, long ts) {
        RpsWrite rpsWrite = JSON.parseObject(value, RpsWrite.class);
        rpsWrite.setTs(ts);
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest = new IndexRequest(topic());
        indexRequest.id(rpsWrite.getKey()).source(JSON.toJSONString(rpsWrite), XContentType.JSON);
        bulkRequest.add(indexRequest);
        try {
            // 开始写入
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if(bulkResponse.hasFailures()){
                throw new Exception("处理数据失败");
            }
            log.info("处理完成");
        }catch (Exception e){
            log.error("ES写入失败: {}", e);
        }finally {
            bulkRequest = null;
        }
    }
}
