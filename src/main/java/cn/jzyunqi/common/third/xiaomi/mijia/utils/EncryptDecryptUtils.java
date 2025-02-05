package cn.jzyunqi.common.third.xiaomi.mijia.utils;

import cn.jzyunqi.common.utils.CollectionUtilPlus;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import cn.jzyunqi.common.utils.IOUtilPlus;
import cn.jzyunqi.common.utils.RandomUtilPlus;
import cn.jzyunqi.common.utils.StringUtilPlus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
public class EncryptDecryptUtils {

    public static String generateNonce(long timeDiff) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putLong(RandomUtilPlus.Number.randomLong(0, Long.MAX_VALUE));
        buffer.putInt((int) ((System.currentTimeMillis() + timeDiff) / 60000));
        return String.valueOf(DigestUtilPlus.Base64.encodeBase64String(buffer.array()));
    }

    public static String decrypt(String responseBody, String security, String nonce, boolean gzipFormat) {
        //计算出key
        byte[] rc4Key = DigestUtilPlus.Base64.decodeBase64(getRc4Key(security, nonce));
        //初始化算法
        Rc4Algorithms rc4 = new Rc4Algorithms(rc4Key);
        rc4.compute(getEmptyBytes());//空算一次
        //开始解密
        byte[] decryptBytes = DigestUtilPlus.Base64.decodeBase64(responseBody);
        rc4.compute(decryptBytes);
        if (!gzipFormat) {
            return new String(decryptBytes, StandardCharsets.UTF_8);
        }
        try (InputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decryptBytes))) {
            return IOUtilPlus.toString(gzipInputStream, StringUtilPlus.UTF_8);
        } catch (IOException e) {
            return responseBody;
        }
    }

    public static void encrypt(String method, String path, Map<String, String> requestParams, String security, String nonce) {
        //计算出key
        String rc4Key = getRc4Key(security, nonce);
        byte[] rc4KeyByte = DigestUtilPlus.Base64.decodeBase64(getRc4Key(security, nonce));
        //初始化算法
        Rc4Algorithms rc4 = new Rc4Algorithms(rc4KeyByte);
        rc4.compute(getEmptyBytes());//空算一次

        //一次签名
        String rc4Hash = sign(method, path, requestParams, rc4Key);
        //一次加入参数
        requestParams.put("rc4_hash__", rc4Hash);

        //加密
        requestParams.replaceAll((key, value) -> {
            byte[] contentBytes = value.getBytes(StringUtilPlus.UTF_8);
            rc4.compute(contentBytes);
            return DigestUtilPlus.Base64.encodeBase64String(contentBytes);
        });

        //二次签名
        String signature = sign(method, path, requestParams, rc4Key);
        //二次加入参数
        requestParams.put("signature", signature);
        requestParams.put("_nonce", nonce);
        requestParams.put("ssecurity", security);
    }

    private static byte[] getEmptyBytes() {
        byte[] emptyBytes = new byte[1024];
        Arrays.fill(emptyBytes, (byte) 0);
        return emptyBytes;
    }

    private static String sign(String method, String path, Map<String, String> requestParamMap, String rc4Key) {
        String needSign = StringUtilPlus.joinWith(StringUtilPlus.AND,
                method.toUpperCase(),
                path,
                CollectionUtilPlus.Map.getUrlParam(requestParamMap, true, true, false),
                rc4Key);
        return DigestUtilPlus.SHA.sign(needSign, DigestUtilPlus.SHAAlgo._1, true);
    }

    private static String getRc4Key(String security, String nonce) {
        byte[] securityByte = DigestUtilPlus.Base64.decodeBase64(security);
        byte[] nonceByte = DigestUtilPlus.Base64.decodeBase64(nonce);
        return DigestUtilPlus.SHA.sign(CollectionUtilPlus.Array.addAll(securityByte, nonceByte), DigestUtilPlus.SHAAlgo._256, true);
    }
}
