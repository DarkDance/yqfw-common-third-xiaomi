package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.feature.redis.RedisHelper;
import cn.jzyunqi.common.third.xiaomi.account.model.ServerTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.account.model.UserTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchangeWrapper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import cn.jzyunqi.common.third.xiaomi.common.constant.XiaomiCache;
import cn.jzyunqi.common.third.xiaomi.mijia.MijiaCoreApiProxy;
import cn.jzyunqi.common.third.xiaomi.mijia.utils.EncryptDecryptUtils;
import cn.jzyunqi.common.third.xiaomi.mijia.utils.Rc4Algorithms;
import cn.jzyunqi.common.utils.CollectionUtilPlus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author wiiyaya
 * @since 2024/9/24
 */
@Configuration
@Slf4j
public class XiaomiConfig {
    private final String userAgent = "Android-12-9.10.701-HONOR-BVLAN00-346-5F27795FFAF1BDAF8660B6832A4038E1412FB383--AC3497B7FA1C8BF8EAA8AA28DCA83AC3-CFCD208495D565EF66E7DFF9F98764DA-SmartHome-MI_APP_STORE-5F27795FFAF1BDAF8660B6832A4038E1412FB383|E32BBECAC33AC6133C44C0509CFA25ED62D06AB5|-64 APP/xiaomi.smarthome APPV/79213 MK/QlZMLUFOMDA= SDKV/5.3.0.release.40 PassportSDK/5.3.0.release.40 passport-ui/5.3.0.release.40 XiaomiAccountSSO/5.3.0.release.40";
    private final String deviceId = "android_e9428fb5-489d-4442-8eef-1b775d0e7d38";

    @Bean
    @ConditionalOnMissingBean
    public XiaomiHttpExchangeWrapper xiaomiHttpExchangeWrapper() {
        return new XiaomiHttpExchangeWrapper();
    }

    @Bean
    public XiaomiClient xiaomiClient(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.clone()
                .defaultHeader("User-Agent", userAgent)
                .defaultCookie("deviceId", deviceId)
                .build();
        return new XiaomiClient(webClient);
    }

    @Bean
    public AccountApiProxy accountApiProxy(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.clone()
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (body.startsWith("&&&START&&&")) {
                                        return Mono.just(clientResponse.mutate().body(body.substring(11)).build());
                                    } else {
                                        return Mono.just(clientResponse);
                                    }
                                })))
                .defaultHeader("User-Agent", userAgent)
                .defaultCookie("deviceId", deviceId)
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        webClientAdapter.setBlockTimeout(Duration.ofSeconds(5));
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();
        return factory.createClient(AccountApiProxy.class);
    }

    @Bean
    public MijiaCoreApiProxy mijiaCoreApiProxy(WebClient.Builder webClientBuilder, XiaomiClientConfig xiaomiClientConfig, RedisHelper redisHelper, Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        WebClient webClient = webClientBuilder.clone()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .jackson2JsonDecoder(new Jackson2JsonDecoder(jackson2ObjectMapperBuilder.build(),
                                MediaType.APPLICATION_JSON,
                                new MediaType("application", "*+json"),
                                MediaType.APPLICATION_NDJSON,
                                MediaType.TEXT_PLAIN
                        )))
                .filter((clientRequest, next) -> {
                    UserTokenRedisDto userToken = (UserTokenRedisDto) redisHelper.vGet(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, xiaomiClientConfig.getAccount());
                    ServerTokenRedisDto mijiaToken = userToken.getServerTokenMap().get("mijia");

                    String method = clientRequest.method().toString();
                    String path = clientRequest.url().getPath().replace("/app", "");
                    String serverToken = mijiaToken.getServerToken();
                    String serverSecurity = mijiaToken.getServerSecurity();

                    EncryptDecryptUtils encryptDecryptUtils = new EncryptDecryptUtils();
                    String nonce = encryptDecryptUtils.toSpecialString(300);
                    String rc4Key = encryptDecryptUtils.getRc4Key(serverSecurity, nonce);

                    String cookie = "cUserId=" + userToken.getEncryptedUserId() + "; yetAnotherServiceToken=" + serverToken + "; serviceToken=" + serverToken + "; timezone_id=Asia/Shanghai; timezone=GMT%2B08%3A00; is_daylight=0; dst_offset=0; channel=MI_APP_STORE; countryCode=CN; locale=zh_CN";

                    ClientRequest newRequest = ClientRequest.create(clientRequest.method(), clientRequest.url())
                            .header(HttpHeaders.USER_AGENT, userAgent)
                            //.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                            .header(HttpHeaders.COOKIE, cookie)
                            .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                            .header("MIOT-ENCRYPT-ALGORITHM", "ENCRYPT-RC4")
                            .header("X-XIAOMI-PROTOCAL-FLAG-CLI", "PROTOCAL-HTTP2")
                            .header("MIOT-ACCEPT-ENCODING", "GZIP")
                            .body((outputMessage, context) -> {
                                return clientRequest.body().insert(new ClientHttpRequestDecorator(outputMessage) {
                                    @Override
                                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                        getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                                        Mono<DataBuffer> mono = DataBufferUtils.join(body).map(dataBuffer -> {
                                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(bytes);
                                            String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                                            bodyStr = bodyStr.replaceAll(" ", "");
                                            bodyStr = bodyStr.replaceAll("\n", "");

                                            Map<String, String> requestParams = new TreeMap<>();
                                            requestParams.put("data", bodyStr);

                                            //一次签名
                                            Rc4Algorithms rc4Algorithms = new Rc4Algorithms(rc4Key);
                                            String rc4Hash = EncryptDecryptUtils.sign(method, path, requestParams, rc4Key);
                                            requestParams.put("rc4_hash__", rc4Hash);
                                            requestParams.replaceAll((key, value) -> rc4Algorithms.encrypt(value));
                                            //二次签名
                                            String signature = EncryptDecryptUtils.sign(method, path, requestParams, rc4Key);
                                            requestParams.put("signature", signature);
                                            requestParams.put("_nonce", nonce);
                                            requestParams.put("ssecurity", serverSecurity);

                                            requestParams.forEach((key, value) -> log.info("{} = {}", key, value));

                                            String bodyStrWithParams = CollectionUtilPlus.Map.getUrlParam(requestParams, false, true, true);
                                            log.info("bodyStrWithParams: {}", bodyStrWithParams);
                                            log.info("cookie: {}", cookie);

                                            DataBufferFactory dataBufferFactory = outputMessage.bufferFactory();
                                            return dataBufferFactory.wrap(bodyStrWithParams.getBytes(StandardCharsets.UTF_8));
                                        });
                                        return super.writeWith(mono);
                                    }
                                }, context);
                            })
                            .build();
                    return next.exchange(newRequest)
                            .flatMap(clientResponse -> clientResponse
                                    .bodyToMono(String.class)
                                    .flatMap(encryptedBody -> {
                                        List<String> gzipFormatList = clientResponse.headers().header("MIOT-CONTENT-ENCODING");
                                        boolean gzipFormat = false;
                                        if (CollectionUtilPlus.Collection.isNotEmpty(gzipFormatList)) {
                                            gzipFormat = "GZIP".equals(gzipFormatList.get(0));
                                        }
                                        Rc4Algorithms rc4Algorithms = new Rc4Algorithms(rc4Key);
                                        String realBody = rc4Algorithms.decrypt(encryptedBody, gzipFormat);
                                        return Mono.just(clientResponse.mutate().body(realBody).build());
                                    })
                            );
                })
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        webClientAdapter.setBlockTimeout(Duration.ofSeconds(5));
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();
        return factory.createClient(MijiaCoreApiProxy.class);
    }
}
