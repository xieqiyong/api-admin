/*
 * Created by zhangzxiang91@gmail.com on 2021/06/16.
 */
package com.hz.api.admin.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/16.
 */
@Slf4j
public class ConfigureHelper {

	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>();

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
	}

	/**
	 * <pre>
	 * 判断是否是基础类型
	 * 基础类型：boolean,char,byte,short,int,long,float,double
	 * 扩展类型：String, java.util.Date, BigDecimal
	 */
	private static boolean isSupportType(Class<?> type) {
		return primitiveWrapperTypeMap.containsKey(type) || primitiveWrapperTypeMap.containsValue(type) || type == String.class || type == Date.class
				|| type == BigDecimal.class;
	}

	/**
	 * <pre>
	 * 将字符串转换成目标类型
	 *
	 * 仅支持基础数据类型转换（boolean,char,byte,short,int,long,float,double,String,Date,BigDecimal）
	 * Date类型支持的格式有：毫秒的Long值、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd等格式
	 */
	public static Object transferValue(Class<?> type, String value) throws IllegalArgumentException {
		if (value == null || (type != String.class && StringUtils.isBlank(value))) {
			return null;
		}

		if (type == char.class || Character.class.isAssignableFrom(type)) {
			if (value.length() != 1) {
				throw new IllegalArgumentException("无法将 [" + value + "] 转换为字符 char 类型");
			}
			return value.charAt(0);
		}
		if (Date.class.isAssignableFrom(type)) {
			long time = NumberUtils.toLong(value);
			if (time > 0) {
				return new Date(time);
			}
			try {
				return DateUtils.parseDate(value, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
			} catch (ParseException ignored) {
			}
			throw new IllegalArgumentException("无法将 [" + value + "] 转换为日期 Date 类型");
		}
		try {
			PropertyEditorManager.registerEditor(BigDecimal.class, BigDecimalEditor.class);
			PropertyEditor editor = PropertyEditorManager.findEditor(type);
			editor.setAsText(value);
			return editor.getValue();
		} catch (Throwable e) {
			throw new IllegalArgumentException("无法将 [" + value + "] 转换为 [" + type.getName() + "] 类型", e);
		}
	}

	private static <T> T newInstance(Class<T> type) {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 将以一定前缀的配置项的值，传递到指定POJO形式的对象。<br/>
	 * 目标对象必须是标准 JAVA Bean 格式
	 *
	 * @param keyPrefix 参数Key的前缀，比如：container.mq.KafkaClient.
	 * @param targetClass 返回的配置对象类型
	 */
	public static <T> T loadToObject(Map<String, String> props, String keyPrefix, Class<T> targetClass) {
		return loadToObject(props, keyPrefix, newInstance(targetClass));
	}

	/**
	 * 将以一定前缀的配置项的值，传递到指定POJO形式的对象<br/>
	 * 目标对象必须是标准 JAVA Bean 格式
	 *
	 * @param keyPrefix 参数Key的前缀，比如：container.mq.KafkaClient.
	 * @param target 配置实例对象
	 * @return 返回的配置对象类型
	 */
	public static <T> T loadToObject(Map<String, String> props, String keyPrefix, T target) {
		if (!StringUtils.endsWith(keyPrefix, ".")) {
			throw new IllegalArgumentException("配置项转换到对象，必须指定Key前缀，并以 . 号结尾");
		}

		// 通过方法找配置中心的配置项
		Method[] methods = target.getClass().getMethods();
		for (Method method : methods) {
			if (!StringUtils.startsWith(method.getName(), "set") || method.getParameterTypes().length != 1 || !Modifier
					.isPublic(method.getModifiers())) {
				continue;
			}

			String property = StringUtils.uncapitalize(StringUtils.removeStart(method.getName(), "set"));
			Class<?> valueClazz = method.getParameterTypes()[0];
			Object value;

			// 所支持的简单类型，直接进行转换值
			if (isSupportType(valueClazz)) {
				value = transferValue(valueClazz, props.get(StringUtils.join(keyPrefix, property)));
			} else {
				// 如果是复杂对象
				value = loadToObject(props, StringUtils.join(keyPrefix, property, "."), valueClazz);
			}
			if (value != null) {
				try {
					method.invoke(target, value);
				} catch (Exception e) {
					throw new IllegalStateException("通过方法 " + target.getClass().getName() + "." + method.getName() + " 属性注入失败： " + e.getMessage(),
							e);
				}
			}
		}
		return target;
	}

	public static class BigDecimalEditor extends PropertyEditorSupport {

		public String getJavaInitializationString() {
			Object var = this.getValue();
			return var != null ? var.toString() : "null";
		}

		@Override
		public void setAsText(String var) throws IllegalArgumentException {
			this.setValue(var != null ? new BigDecimal(var) : null);
		}
	}

}
