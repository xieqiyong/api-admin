/*
 * Created by zhangzxiang91@gmail.com on 2021/06/06.
 */
package com.hz.api.admin.netkit.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/06.
 */
public class SystemUtils extends org.apache.commons.lang3.SystemUtils {

	public static String getProp(String key, String defaultValue) {
		// 读取系统属性
		String value = StringUtils.trimToNull(System.getProperty(key));
		return value != null ? value : defaultValue;
	}
}
