package com.hz.api.admin.client.server;

/**
 * @author xieqiyong66@gmail.com
 * @description: ServerDataListener
 * @date 2022/12/15 2:23 下午
 */
public interface ServerDataListener {
    /**
     * 接收服务端数据
     * @param value
     */
    void onData(byte[] value);
}
