/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.rsa;

import android.util.Base64;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import armadillo.studio.common.config.AppConfig;

public final class RSAUtils {

    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static volatile PrivateKey cachedPrivateKey;
    private static volatile PublicKey cachedPublicKey;
    private static volatile KeyFactory cachedKeyFactory;

    private static KeyFactory getKeyFactory() throws Exception {
        if (cachedKeyFactory == null) {
            synchronized (RSAUtils.class) {
                if (cachedKeyFactory == null) {
                    cachedKeyFactory = KeyFactory.getInstance("RSA");
                }
            }
        }
        return cachedKeyFactory;
    }

    public static PublicKey getPublicKey(byte[] keyBytes) throws Exception {
        return getKeyFactory().generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public static PrivateKey getPrivateKey(byte[] keyBytes) throws Exception {
        return getKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static PrivateKey getCachedPrivateKey() throws Exception {
        if (cachedPrivateKey == null) {
            synchronized (RSAUtils.class) {
                if (cachedPrivateKey == null) {
                    byte[] keyBytes = Base64.decode(AppConfig.RSA_PRIVATE_KEY, Base64.NO_WRAP);
                    cachedPrivateKey = getKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                }
            }
        }
        return cachedPrivateKey;
    }

    private static PublicKey getCachedPublicKey() throws Exception {
        if (cachedPublicKey == null) {
            synchronized (RSAUtils.class) {
                if (cachedPublicKey == null) {
                    byte[] keyBytes = Base64.decode(AppConfig.RSA_PUBLIC_KEY.getBytes(), Base64.NO_WRAP);
                    cachedPublicKey = getKeyFactory().generatePublic(new X509EncodedKeySpec(keyBytes));
                }
            }
        }
        return cachedPublicKey;
    }

    @NotNull
    public static byte[] decrypt(@NotNull byte[] encryptedData) throws Exception {
        return decrypt(encryptedData, getCachedPublicKey());
    }

    @NotNull
    public static byte[] decrypt(@NotNull byte[] encryptedData, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream(inputLen);
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    @NotNull
    public static byte[] encrypt(@NotNull byte[] data) throws Exception {
        return encrypt(data, getCachedPrivateKey());
    }

    @NotNull
    public static byte[] encrypt(@NotNull byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream(inputLen);
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }
}
