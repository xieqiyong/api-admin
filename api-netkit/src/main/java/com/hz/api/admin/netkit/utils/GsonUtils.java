/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON转换工具
 * <p>
 * Jackson 框架的高阶应用: https://www.ibm.com/developerworks/cn/java/jackson-advanced-application/index.html
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public final class GsonUtils {
	private static String UTF8="UTF-8";

	private static Gson gson = new Gson();

	private GsonUtils() {

	}

	public static String toString(Object object) {
		try {
			return gson.toJson(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] toBytes(Object object) {
		try {
			return gson.toJson(object).getBytes(UTF8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> parseArray(String text, Class<T> clazz) {
		try {
			JsonArray jsonArray = new JsonParser().parse(text).getAsJsonArray();
			List<T> list = new ArrayList<T>();
			if(jsonArray!=null&&jsonArray.size()!=0){
				for(int i=0;i<jsonArray.size();i++){
					JsonElement jsonElement = jsonArray.get(i);
					list.add(gson.fromJson(jsonElement.toString(), clazz));
				}
			}
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parseObject(String text, Class<T> clazz) {
		try {
			return gson.fromJson(text, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> valueToList(JsonObject jsonNode, Class<T> clazz) {
		try {
			return gson.fromJson(jsonNode.toString(), new TypeToken<List<T>>(){}.getType());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> valueToArray(JsonObject jsonNode, Class<T> clazz) {
		try {
			return gson.fromJson(jsonNode.toString(), new TypeToken<List<T>>(){}.getType());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static <T> T valueToObject(JsonObject jsonNode, Class<T> clazz) {
		try {
			return gson.fromJson(jsonNode.toString(), clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonObject readValue(String text) {
		try {
			return new JsonParser().parse(text).getAsJsonObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
