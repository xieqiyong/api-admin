package com.hz.api.admin.stream.data.enums;


import java.util.ArrayList;
import java.util.List;

public enum EsIndexEnum {

    RPS_SUCCESS("stress_request_success_1s", "stress_request_success_detail"),
    RPS_FAIL("stress_request_fail_1s", "stress_request_fail_detail"),
    RPS("stress_request_1s","stress_request_detail");

    public String getIndex() {
        return index;
    }

    public void setIndex(String index, String topic) {
        this.index = index;
        this.topic = topic;
    }

    private String index;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    private String topic;

    EsIndexEnum(String index, String topic){
        this.index = index;
        this.topic = topic;
    }

    public static List<String> kafkaIndex(){
        List<String> indexs = new ArrayList<>();
        for (EsIndexEnum value : EsIndexEnum.values()) {
            indexs.add(value.index);
        }
        return indexs;
    }
}
