/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.rsa;

import android.util.Base64;

import java.security.PrivateKey;
import java.security.Signature;

import armadillo.studio.common.config.AppConfig;

public class RSASignature {
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    private static volatile PrivateKey cachedPrivateKey;
    private static volatile byte[] cachedMagicBytes;
    private static volatile byte[] cachedAppIdBytes;

    private static PrivateKey getCachedPrivateKey() throws Exception {
        if (cachedPrivateKey == null) {
            synchronized (RSASignature.class) {
                if (cachedPrivateKey == null) {
                    byte[] keyBytes = Base64.decode(AppConfig.RSA_PRIVATE_KEY, Base64.NO_WRAP);
                    cachedPrivateKey = RSAUtils.getPrivateKey(keyBytes);
                }
            }
        }
        return cachedPrivateKey;
    }

    private static byte[] getCachedMagicBytes() {
        if (cachedMagicBytes == null) {
            synchronized (RSASignature.class) {
                if (cachedMagicBytes == null) {
                    cachedMagicBytes = AppConfig.SIGN_MAGIC;
                }
            }
        }
        return cachedMagicBytes;
    }

    private static byte[] getCachedAppIdBytes() {
        if (cachedAppIdBytes == null) {
            synchronized (RSASignature.class) {
                if (cachedAppIdBytes == null) {
                    cachedAppIdBytes = AppConfig.TENCENT_APPID.getBytes();
                }
            }
        }
        return cachedAppIdBytes;
    }

    public static byte[] sign(byte[] content) throws Exception {
        PrivateKey priKey = getCachedPrivateKey();
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(priKey);
        signature.update(content);
        signature.update(getCachedMagicBytes());
        signature.update(getCachedAppIdBytes());
        return signature.sign();
    }
}
