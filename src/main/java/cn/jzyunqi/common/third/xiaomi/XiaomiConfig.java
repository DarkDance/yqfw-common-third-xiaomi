package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.feature.redis.RedisHelper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import cn.jzyunqi.common.third.xiaomi.account.model.ServerTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.account.model.UserTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchangeWrapper;
import cn.jzyunqi.common.third.xiaomi.common.constant.XiaomiCache;
import cn.jzyunqi.common.third.xiaomi.mijia.MijiaApiProxy;
import cn.jzyunqi.common.third.xiaomi.mijia.utils.EncryptDecryptUtils;
import cn.jzyunqi.common.utils.CollectionUtilPlus;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    public MijiaApiProxy mijiaApiProxy(WebClient.Builder webClientBuilder, XiaomiAuthRepository xiaomiAuthRepository, RedisHelper redisHelper, Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
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
                    String account = clientRequest.attribute("_$$account").orElse(xiaomiAuthRepository.getXiaomiAuth(null).getAccount()).toString();
                    UserTokenRedisDto userToken = (UserTokenRedisDto) redisHelper.vGet(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, account);
                    ServerTokenRedisDto mijiaToken = userToken.getServerTokenMap().get("mijia");

                    String method = clientRequest.method().toString();
                    String path = clientRequest.url().getPath().replace("/app", "");
                    String serverToken = mijiaToken.getServerToken();
                    String serverSecurity = mijiaToken.getServerSecurity();
                    String nonce = EncryptDecryptUtils.generateNonce(300);

                    String cookie = "cUserId=" + userToken.getEncryptedUserId() + "; yetAnotherServiceToken=" + serverToken + "; serviceToken=" + serverToken + "; timezone_id=Asia/Shanghai; timezone=GMT%2B08%3A00; is_daylight=0; dst_offset=0; channel=MI_APP_STORE; countryCode=CN; locale=zh_CN";

                    //将原JSON模式的请求转换成form-data模式，并且加密数据后作为一个新的请求处理
                    ClientRequest newRequest = ClientRequest.create(clientRequest.method(), clientRequest.url())
                            .header(HttpHeaders.USER_AGENT, userAgent)
                            .header(HttpHeaders.COOKIE, cookie)
                            .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                            .header("MIOT-ENCRYPT-ALGORITHM", "ENCRYPT-RC4")
                            .header("X-XIAOMI-PROTOCAL-FLAG-CLI", "PROTOCAL-HTTP2")
                            .header("MIOT-ACCEPT-ENCODING", "GZIP")
                            .body((outputMessage, context) -> clientRequest
                                    .body()
                                    .insert(new ClientHttpRequestDecorator(outputMessage) {
                                        @Override
                                        public @NonNull Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                                            //只能在这里将请求改成form-data格式，否则body会使用这个格式来编码，导致格式错误，比如原请求为JSON格式
                                            getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                                            return super.writeWith(DataBufferUtils.join(body).map(dataBuffer -> {
                                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                                dataBuffer.read(bytes);
                                                String bodyStr = new String(bytes, StandardCharsets.UTF_8);

                                                Map<String, String> requestParams = new TreeMap<>();
                                                requestParams.put("data", bodyStr);

                                                EncryptDecryptUtils.encrypt(method, path, requestParams, serverSecurity, nonce);

                                                String bodyStrWithParams = CollectionUtilPlus.Map.getUrlParam(requestParams, false, true, true);
                                                return outputMessage.bufferFactory().wrap(bodyStrWithParams.getBytes(StandardCharsets.UTF_8));
                                            }));
                                        }
                                    }, context))
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
                                        String realBody = EncryptDecryptUtils.decrypt(encryptedBody, serverSecurity, nonce, gzipFormat);
                                        return Mono.just(clientResponse.mutate().body(realBody).build());
                                    })
                            );
                })
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        webClientAdapter.setBlockTimeout(Duration.ofSeconds(5));
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(webClientAdapter).build();
        return factory.createClient(MijiaApiProxy.class);
    }
}
