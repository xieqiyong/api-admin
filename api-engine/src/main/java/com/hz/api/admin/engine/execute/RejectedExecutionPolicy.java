/*
 * Created by zhangzxiang91@gmail.com on 2022/04/25.
 */
package com.hz.api.admin.engine.execute;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/25.
 */
public enum RejectedExecutionPolicy {
	/**
	 * 阻塞等待
	 */
	Block,
	/**
	 * 拒绝
	 */
	Abort,
}
