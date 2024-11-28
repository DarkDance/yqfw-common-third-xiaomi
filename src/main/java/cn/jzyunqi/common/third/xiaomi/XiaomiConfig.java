package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchangeWrapper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author wiiyaya
 * @since 2024/9/24
 */
@Configuration
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
}
