package com.hz.api.admin.netkit.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * <p>加密解密抽象类，封装实际的加解密操作.</p>
 *
 * @author jisonami
 * @since 0.0.1
 */
public abstract class AbstractCryptography {

    /**
     * 表示公钥的字符串常量.
     */
    static final String PUBLIC_KEY = "PublicKey";

    /**
     * 表示私钥的字符串常量.
     */
    static final String PRIVATE_KEY = "PrivateKey";

    /**
     * 加解密配置信息字段.
     */
    private Configuration configuration = new Configuration();

    /**
     * 用于继承的无参构造方法.
     */
    protected AbstractCryptography() {
    }

    /**
     * 用于继承的构造方法.
     *
     * @param configuration 加解密的配置信息
     */
    protected AbstractCryptography(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 获取密码类.
     *
     * @return 密码对象
     */
    private Cipher getCipher() {
        Cipher cipher;
        try {
            if (configuration.getProviderName() != null && !"".equals(configuration.getProviderName())) {
                cipher = Cipher.getInstance(configuration.getCipherAlgorithm(), configuration.getProviderName());
            } else if (configuration.getProvider() != null) {
                cipher = Cipher.getInstance(configuration.getCipherAlgorithm(), configuration.getProvider());
            } else {
                cipher = Cipher.getInstance(configuration.getCipherAlgorithm());
            }
            return cipher;
        } catch (Exception e) {
            throw new EncryptException("获取加密密码类失败: ", e);
        }
    }

    /**
     * 加密操作.
     *
     * @param data 需要加密的数据
     * @param key  密钥对象
     * @return 加密后的数据
     */
    protected byte[] encrypt(final byte[] data, final Key key) {
        Cipher cipher = getCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new EncryptException(String.format("加密失败, 无效的密钥[%s]: ", key.toString()), e);
        }
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new EncryptException("加密数据失败: ", e);
        }
    }

    /**
     * 解密操作.
     *
     * @param data 需要解密的数据
     * @param key  密钥对象
     * @return 解密后的数据
     */
    protected byte[] decrypt(byte[] data, Key key) {
        Cipher cipher = getCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            throw new EncryptException(String.format("解密失败, 无效的密钥[%s]: ", key.toString()), e);
        }
        try {
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new EncryptException("解密数据失败: ", e);
        }
    }


    /**
     * 将二进制key编码成字符串形式.
     *
     * @param key 密钥的二进制形式
     * @return 密钥的字符串形式
     */
    public String encodeKey(final byte[] key) {
        return keyToBase64(key);
    }

    /**
     * 将字符串key解码成二进制形式.
     *
     * @param key 密钥的字符串形式
     * @return 密钥的二进制形式
     */
    public byte[] decodeKey(final String key) {
        return base64ToKey(key);
    }

    /**
     * 将密钥的二进制形式转换成Base64编码形式.
     *
     * @param key 密钥的二进制形式
     * @return 密钥的Base64字符串形式
     */
    private String keyToBase64(final byte[] key) {
        return Base64.encodeBase64String(key);
    }

    /**
     * 将密钥的Base64编码形式转换成二进制形式.
     *
     * @param key 密钥的Base64字符串形式
     * @return 密钥的二进制形式
     */
    private byte[] base64ToKey(final String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * getter method.
     * @return configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

}
