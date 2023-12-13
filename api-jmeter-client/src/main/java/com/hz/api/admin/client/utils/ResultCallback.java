
package com.hz.api.admin.client.utils;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/15.
 */
public interface ResultCallback<T> {

	/**
	 * 完成回调接口
	 */
	void onCompletion(T result, Throwable ex);
}
