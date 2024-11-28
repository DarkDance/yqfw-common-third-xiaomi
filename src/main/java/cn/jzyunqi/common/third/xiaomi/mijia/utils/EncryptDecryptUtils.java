package cn.jzyunqi.common.third.xiaomi.mijia.utils;

import cn.jzyunqi.common.third.xiaomi.mijia.model.RequestParam;
import cn.jzyunqi.common.utils.CollectionUtilPlus;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import cn.jzyunqi.common.utils.RandomUtilPlus;
import cn.jzyunqi.common.utils.StringUtilPlus;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
public class EncryptDecryptUtils {

    //"GZIP".equals(headers.get("MIOT-CONTENT-ENCODING"))
    public String decrypt(String responseBody, String security, String nonce, boolean gzipFormat) {
        String rc4Key = getRc4Key(security, nonce);
        Rc4Algorithms myAlgorithms = new Rc4Algorithms(rc4Key);
        return myAlgorithms.decrypt(responseBody, gzipFormat);
    }

    //netRequest.headers.add(new KeyValuePair("MIOT-ENCRYPT-ALGORITHM", "ENCRYPT-RC4"));
    //netRequest.headers.add(new KeyValuePair("Accept-Encoding", "identity"));
    public RequestParam prepareRquestParam(String method, String path, Map<String, String> requestParams, String security, int timeDiff, String nonce) {
        //获取rck和算法
        //String nonce = toSpecialString(timeDiff);
        String rc4Key = getRc4Key(security, nonce);
        Rc4Algorithms rc4Algorithms = new Rc4Algorithms(rc4Key);

        //一次签名
        TreeMap<String, String> signatureParams = new TreeMap<>(requestParams);
        String rc4Hash = sign(method, path, signatureParams, rc4Key);
        signatureParams.put("rc4_hash__", rc4Hash);
        signatureParams.replaceAll((key, value) -> rc4Algorithms.encrypt(value));
        //二次签名
        String signature = sign(method, path, signatureParams, rc4Key);

        //组装请求参数
        RequestParam requestParam = new RequestParam();
        requestParam.setData(signatureParams.get("data"));
        requestParam.setSignature(signature);
        requestParam.setNonce(nonce);
        requestParam.setRc4Hash(signatureParams.get("rc4_hash__"));
        requestParam.setSecurity(security);
        return requestParam;
    }

    public static String sign(String method, String path, Map<String, String> requestParamMap, String rc4Key) {
        String needSign =StringUtilPlus.joinWith(StringUtilPlus.AND,
                method.toUpperCase(),
                path,
                CollectionUtilPlus.Map.getUrlParam(requestParamMap, true, true, false),
                rc4Key);
        System.out.println("needSign:" + needSign);
        return DigestUtilPlus.SHA.sign(needSign, DigestUtilPlus.SHAAlgo._1, true);
    }

    //private static String toSpecialString(long timeDiff) {
    //    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    //    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    //    try {
    //        dataOutputStream.writeLong(RandomUtilPlus.Number.nextLong());
    //        dataOutputStream.writeInt((int) ((System.currentTimeMillis() + timeDiff) / 60000));
    //        dataOutputStream.flush();
    //    } catch (IOException unused) {
    //    }
    //    return String.valueOf(DigestUtilPlus.Base64.encodeBase64String(byteArrayOutputStream.toByteArray()));
    //}

    public String getRc4Key(String security, String nonce) {
        byte[] securityByte = DigestUtilPlus.Base64.decodeBase64(security);
        byte[] nonceByte = DigestUtilPlus.Base64.decodeBase64(nonce);
        return DigestUtilPlus.SHA.sign(join(securityByte, nonceByte), DigestUtilPlus.SHAAlgo._256, true);
    }

    public String toSpecialString(long timeDiff) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putLong(RandomUtilPlus.Number.nextLong());
        buffer.putInt((int) ((System.currentTimeMillis() + timeDiff) / 60000));
        return String.valueOf(DigestUtilPlus.Base64.encodeBase64String(buffer.array()));
    }

    public static long[] fromSpecialString(String specialString) {
        byte[] decodedBytes = DigestUtilPlus.Base64.decodeBase64(specialString);
        ByteBuffer buffer = ByteBuffer.wrap(decodedBytes);
        long randomLong = buffer.getLong();
        int minutesSinceEpoch = buffer.getInt();
        long timeInMillis = minutesSinceEpoch * 60000L;
        long currentTime = System.currentTimeMillis();
        long timeDiff = timeInMillis - currentTime;
        return new long[]{randomLong, timeDiff};
    }

    public byte[] join(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[bArr.length + bArr2.length];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }
}
