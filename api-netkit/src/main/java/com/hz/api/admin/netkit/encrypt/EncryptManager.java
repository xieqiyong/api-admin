package com.hz.api.admin.netkit.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class EncryptManager {

	// channel与密钥Key射关系，key=channelId, value={}
	private static final Map<String, String>   channelId2EncryptKeyMap = new ConcurrentHashMap<String, String>();
	private static final AtomicBoolean         inited                  = new AtomicBoolean();
	private static       SymmetricCryptography cryptography;

	static {
		init();
	}

	public static void init() {
		if (!inited.compareAndSet(false, true)) {
			return;
		}

		Configuration configuration = new Configuration();
		configuration.setKeyAlgorithm(Algorithms.AES).setCipherAlgorithm(Algorithms.AES_ECB_PKCS5PADDING).setKeySize(128);
		cryptography = new SymmetricCryptography(configuration);
	}

	/**
	 * 生成密钥Key
	 */
	public static String genEncryptKey(String channelId) {
		if (channelId2EncryptKeyMap.containsKey(channelId)) {
			return channelId2EncryptKeyMap.get(channelId);
		}
		String encryptKey = cryptography.initKeyStr();
		channelId2EncryptKeyMap.put(channelId, encryptKey);
		return encryptKey;
	}

	/**
	 * 绑定channel加密
	 */
	public static void bindEncrypt(String channelId, String encryptKey) {
		channelId2EncryptKeyMap.put(channelId, encryptKey);
	}

	/**
	 * 移除加密channel绑定
	 */
	public static void removeEncrypt(String channelId) {
		channelId2EncryptKeyMap.remove(channelId);
	}

	/**
	 * 判断是否需要加密
	 */
	public static boolean isEncrypt(String channelId) {
		return channelId2EncryptKeyMap.containsKey(channelId);
	}

	/**
	 * 数据加密
	 */
	public static byte[] encrypt(String channelId, byte[] dataBytes) {
		String encryptKey = channelId2EncryptKeyMap.get(channelId);
		if (StringUtils.isBlank(encryptKey)) {
			throw new EncryptException("ChannelId:'" + channelId + "' encryptKey not found.");
		}

		return cryptography.encrypt(dataBytes, encryptKey);
	}

	/**
	 * 数据解密
	 */
	public static byte[] decrypt(String channelId, byte[] dataBytes) {
		String encryptKey = channelId2EncryptKeyMap.get(channelId);
		if (StringUtils.isBlank(encryptKey)) {
			throw new EncryptException("ChannelId:'" + channelId + "' encryptKey not found.");
		}

		return cryptography.decrypt(dataBytes, encryptKey);
	}
}
