package com.hz.api.admin.stream.write;

public interface WriteEsManager {

    public String topic();


    public void writeEs(String value, long ts);

}
