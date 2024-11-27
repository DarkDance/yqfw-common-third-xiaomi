package cn.jzyunqi.common.third.xiaomi.mijia.utils;

import cn.jzyunqi.common.third.xiaomi.mijia.model.RequestParam;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
@Slf4j
public class MyEncryptDecryptUtils extends EncryptDecryptUtils {

    @Override
    public final String decrypt(String responseBody, String security, String nonce, boolean gzipFormat) {
        return responseBody;
    }

    @Override
    public RequestParam prepareRquestParam(String method, String path, Map<String, String> requestParams, String security, int timeDiff) {
        //获取rck和算法
        String nonce = toSpecialString(timeDiff);
        String rc4Key = getRc4Key(security, nonce);

        //组装签名参数
        StringBuilder needSign = new StringBuilder();
        needSign.append(path);
        needSign.append(rc4Key);
        needSign.append(nonce);
        needSign.append(requestParams.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining()));
        needSign.append("data=");

        //签名
        try {
            String sign = DigestUtilPlus.Mac.sign(needSign.toString(), DigestUtilPlus.Base64.decodeBase64(rc4Key), DigestUtilPlus.MacAlgo.H_SHA256, true);
            //组装请求参数
            RequestParam requestParam = new RequestParam();
            requestParam.setRequestParams(requestParams);
            requestParam.setData(null);
            requestParam.setSignature(sign);
            requestParam.setNonce(nonce);
            requestParam.setSecurity(security);
            return requestParam;
        } catch (Exception e) {
            log.error("sign error: ", e);
            return null;
        }
    }
}
