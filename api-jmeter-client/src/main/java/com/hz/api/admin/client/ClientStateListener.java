/*
 * Created by zhangzxiang91@gmail.com on 2021/07/06.
 */
package com.hz.api.admin.client;

import com.hz.api.admin.model.enums.ClientState;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/07/06.
 */
public interface ClientStateListener {

	void stateChanged(DataServerClient client, ClientState newState);

}
