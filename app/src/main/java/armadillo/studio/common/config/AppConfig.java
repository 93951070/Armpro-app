/*
 * Copyright (c) 2021. Armadillo
 * 业务域名与配置集中管理（从XML资源迁移至Java代码）
 */

package armadillo.studio.common.config;

public final class AppConfig {
    private AppConfig() {}

    // 服务器主机地址
    public static final String HOST = "154.222.26.8";

    // 端口范围
    public static final int START_PORT = 10000;
    public static final int END_PORT = 10020;

    // 上传大小限制(MB)
    public static final int UPLOAD_MAX = 200;

    // RSA公钥
    public static final String RSA_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCc7GjoLO7Ebk4WMRXROR6pim+bybzATWSHpUV2U13GLEPAgRw8BFYyhWnD4k9376mUss78V8aUvoG8X8xNydCORmhJEg1rBkjHIzTcCd+jJhbYXcWE3wpEem2MLHxrigXaPOhRrk4Q5m1y+vLU/2tB7UIAqXm0MX4hBnU5bjgfaQIDAQAB";

    // RSA私钥
    public static final String RSA_PRIVATE_KEY =
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJzsaOgs7sRuThYxFdE5HqmKb5vJvMBNZIelRXZTXcYsQ8CBHDwEVjKFacPiT3fvqZSyzvxXxpS+gbxfzE3J0I5GaEkSDWsGSMcjNNwJ36MmFthdxYTfCkR6bYwsfGuKBdo86FGuThDmbXL68tT/a0HtQgCpebQxfiEGdTluOB9pAgMBAAECgYAT7mMUukBJask4QFvJCzw9xHuQifsTYcEXCYLdGJGcjhq85Kk1Zkecex2H58K4NRSQ8nUfz/ZFBj1sM667Ypq+hFTdxAVYXNE3tNxV8bS4wBAI6z4V96P+7mmXp+/GCublxW9AyPXHWDfgyr2MX9CrDQrZfTc+NAPKUYyNspH7UQJBANmHuNEy4t+Vy4YoDPM6B/uIMq+r1RsYWNgvKzWTpaT+BXf8P1YURRMNDW5mv6I7l1gEzqLRhs7qAgKVHW7idEcCQQC4rNTetnnQicF8M/chJPococ7KTVEclJgH1xpto5N/gxMBidXQNleir3dOWetffdFH7OjzkRFcHtA+GPd88hbPAkAUqyupXO9nlSEQby/D0Ii/opJGVMpb17VhXSH65jt/8M7uNp6B/E0P4VacXdDphkX8DADiQYxQjrWAL1Drn0KzAkA8jl/XD9rt6N4LTjbxJWDmVqCqcaPxTUB1AbHm1cad/2sSDBKSjd86t3LVvGKUE3u2rBbPF3irzmbWlfVBgVi9AkAOvjfqYb939tDJ0LozfIxc6ji1mlKK15G9qUiHH0vJTMX3Yr/j8BMM8QaSEWJRquWVJ2gB0JxoaxGQioyASKS0";

    // 腾讯APPID
    public static final String TENCENT_APPID = "1110300103";

    // 邮箱
    public static final String CONTACT_EMAIL = "PanGolin520@Gmail.com";

    // 过滤字符集
    public static final String FILTER_VCODE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // 默认颜色
    public static final int DEFAULT_COLOR = -16777216;

    // HTTP下载基础URL
    public static final String HTTP_BASE_URL = "http://" + HOST + ":8000";

    // Google登录失败提示
    public static final String GOOGLE_LOGIN_FAILED = "Google sign in failed";

    // 签名标识
    public static final byte[] SIGN_MAGIC = "Armadillo".getBytes();

    // 网络错误提示
    public static final String CONNECTION_FAIL = "Connection failure";
    public static final String REQUEST_EXCEPTION = "Data request exception";
    public static final String PARSING_FAILED = "Data parsing failed";
}
