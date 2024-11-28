package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.feature.redis.RedisHelper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import cn.jzyunqi.common.third.xiaomi.account.model.ServerTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.account.model.ServiceLoginData;
import cn.jzyunqi.common.third.xiaomi.account.model.UserTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.common.constant.XiaomiCache;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import cn.jzyunqi.common.utils.StringUtilPlus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 公众号客户端
 *
 * @author wiiyaya
 * @since 2024/9/23
 */
@Slf4j
public class XiaomiClient {

    @Resource
    private AccountApiProxy accountApiProxy;

    @Resource
    private XiaomiClientConfig xiaomiClientConfig;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private WebClient webClient;

    public XiaomiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public final Account account = new Account();

    public class Account {
        public ServiceLoginData serviceLogin() throws BusinessException {
            String serviceId = "mijia";
            UserTokenRedisDto userTokenRedisDto = (UserTokenRedisDto) redisHelper.vGet(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, xiaomiClientConfig.getAccount());
            if (userTokenRedisDto == null) {
                userTokenRedisDto = new UserTokenRedisDto();
                userTokenRedisDto.setServerTokenMap(new HashMap<>());
            }
            ServiceLoginData loginData;
            try {
                if(userTokenRedisDto.getUserId() == null){
                    loginData = accountApiProxy.serviceLogin(xiaomiClientConfig.getAccount(), serviceId);
                }else{
                    loginData = accountApiProxy.serviceLogin(userTokenRedisDto.getUserId(), userTokenRedisDto.getPassToken(), serviceId);
                }
            } catch (BusinessException e) {
                ServiceLoginData errorData = (ServiceLoginData) e.getArguments()[2];
                loginData = accountApiProxy.serviceLoginAuth2(
                        errorData.getQueryStr(),
                        errorData.getCallback(),
                        errorData.getSign(),
                        xiaomiClientConfig.getAccount(),
                        DigestUtilPlus.MD5.sign(xiaomiClientConfig.getPassword(), false).toUpperCase()
                );
            }
            userTokenRedisDto.setUserId(loginData.getUserId());
            userTokenRedisDto.setPassToken(loginData.getPassToken());
            redisHelper.vPut(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, xiaomiClientConfig.getAccount(), userTokenRedisDto);

            String needSign = "nonce=" + loginData.getNonce() + '&' + loginData.getServerSecurity();
            String sign = DigestUtilPlus.SHA.sign(needSign, DigestUtilPlus.SHAAlgo._1, true);

            String url = loginData.getLocation() + "&clientSign=" + URLEncoder.encode(sign, StringUtilPlus.UTF_8);
            ResponseEntity<String> response = webClient.get()
                    .uri(URI.create(url))//必须这样，否则会出现多次encode
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response != null) {
                List<String> cookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
                if (cookieHeaders != null) {
                    ServiceLoginData finalLoginData = loginData;
                    UserTokenRedisDto finalUserTokenRedisDto = userTokenRedisDto;
                    cookieHeaders.stream()
                            .map(HttpCookie::parse)
                            .flatMap(Collection::stream)
                            .filter(httpCookie -> httpCookie.getName().equals("serviceToken"))
                            .findFirst()
                            .map(httpCookie -> {
                                ServerTokenRedisDto serverTokenRedisDto = new ServerTokenRedisDto();
                                serverTokenRedisDto.setServerId(serviceId);
                                serverTokenRedisDto.setServerSecurity(finalLoginData.getServerSecurity());
                                serverTokenRedisDto.setServerToken(httpCookie.getValue());
                                return serverTokenRedisDto;
                            }).ifPresent(serverTokenRedisDto -> {
                                finalUserTokenRedisDto.getServerTokenMap().put(serviceId, serverTokenRedisDto);
                                redisHelper.vPut(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, xiaomiClientConfig.getAccount(), finalUserTokenRedisDto);
                            });
                    ;
                }
            }
            return loginData;
        }
    }

}
