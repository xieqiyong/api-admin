package com.hz.api.admin.netkit.encrypt;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * <p>Created by jisonami on 2016/10/14.</p>
 * <p>处理对称加密算法相关操作，包括密钥生成，密钥还原，加解密相关操作</p>
 * <p>默认使用AES算法和UTF-8编码</p>
 * <p>可选算法包括DES、DESede、AES、RC2、RC4、Blowfish等</p>
 * <p>使用Bouncy Castle组件包还可支持IDEA算法</p>
 *
 * @author jisonami
 * @since 0.0.1
 */
public class SymmetricCryptography extends AbstractCryptography {

    public SymmetricCryptography() {
        getConfiguration().setKeyAlgorithm(Algorithms.AES).setCipherAlgorithm(Algorithms.AES_ECB_PKCS5PADDING).setKeySize(Algorithms.KEYSIZE_128);
    }

    public SymmetricCryptography(Configuration configuration) {
        super(configuration);
    }

    /**
     * 获取对称密钥生成器
     *
     * @return 对称密钥生成器对象
     */
    private KeyGenerator getKeyGenerator() {
        KeyGenerator keyGenerator = null;
        try {
            if (getConfiguration().getProviderName() != null && !"".equals(getConfiguration().getProviderName())) {
                keyGenerator = KeyGenerator.getInstance(getConfiguration().getKeyAlgorithm(), getConfiguration().getProviderName());
            } else if (getConfiguration().getProvider() != null) {
                keyGenerator = KeyGenerator.getInstance(getConfiguration().getKeyAlgorithm(), getConfiguration().getProvider());
            } else {
                keyGenerator = KeyGenerator.getInstance(getConfiguration().getKeyAlgorithm());
            }
            return keyGenerator;
        } catch (Exception e) {
            throw new EncryptException("获取密钥生成器失败: ", e);
        }
    }

    /**
     * 生成一个密钥
     *
     * @return 密钥的二进制形式
     */
    public byte[] initKey() {
        KeyGenerator kg = getKeyGenerator();
        kg.init(getConfiguration().getKeySize());
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 生成一个密钥string
     *
     * @return 密钥的二进制形式
     */
    public String initKeyStr() {
        KeyGenerator kg = getKeyGenerator();
        kg.init(getConfiguration().getKeySize());
        SecretKey secretKey = kg.generateKey();
        return encodeKey(secretKey.getEncoded());
    }

    /**
     * 转换密钥
     *
     * @param key 密钥的二进制形式
     * @return Key 密钥对象
     */
    public Key toSecretKey(byte[] key) {
        return new SecretKeySpec(key, getConfiguration().getKeyAlgorithm());
    }

    /**
     * 加密操作
     *
     * @param data 需要加密的数据
     * @param key  密钥的二进制形式
     * @return 加密后的数据
     */
    public byte[] encrypt(byte[] data, byte[] key) {
        Key k = toSecretKey(key);
        return this.encrypt(data, k);
    }

    /**
     * 加密操作
     *
     * @param data 需要加密的数据
     * @param key  密钥的二进制形式
     * @return 加密后的数据
     */
    public byte[] encrypt(byte[] data, String key) {
        return this.encrypt(data, this.decodeKey(key));
    }

    /**
     * 解密操作
     *
     * @param data 需要解密的数据
     * @param key  密钥的二进制形式
     * @return 解密后的数据
     */
    public byte[] decrypt(byte[] data, String key) {
        return decrypt(data, decodeKey(key));

    }

    /**
     * 解密操作
     *
     * @param data 需要解密的数据
     * @param key  密钥的二进制形式
     * @return 解密后的数据
     */
    public byte[] decrypt(byte[] data, byte[] key) {
        Key k = toSecretKey(key);
        return this.decrypt(data, k);
    }

}
