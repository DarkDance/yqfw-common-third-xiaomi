package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.feature.redis.RedisHelper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import cn.jzyunqi.common.third.xiaomi.account.enums.MiServer;
import cn.jzyunqi.common.third.xiaomi.account.model.ServerTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.account.model.ServiceLoginData;
import cn.jzyunqi.common.third.xiaomi.account.model.UserTokenRedisDto;
import cn.jzyunqi.common.third.xiaomi.common.constant.XiaomiCache;
import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV2;
import cn.jzyunqi.common.third.xiaomi.mijia.MijiaApiProxy;
import cn.jzyunqi.common.third.xiaomi.mijia.MijiaCoreApiProxy;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceData;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceDataRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceParam;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceSearchParam;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import cn.jzyunqi.common.utils.StringUtilPlus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 公众号客户端
 *
 * @author wiiyaya
 * @since 2024/9/23
 */
@Slf4j
public class XiaomiClient {

    private final WebClient webClient;

    @Resource
    private MijiaApiProxy mijiaApiProxy;

    @Resource
    private MijiaCoreApiProxy mijiaCoreApiProxy;

    @Resource
    private AccountApiProxy accountApiProxy;

    @Resource
    private XiaomiClientConfig xiaomiClientConfig;

    @Resource
    private RedisHelper redisHelper;

    public XiaomiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public final Account account = new Account();

    public final MijiaCore mijiaCore = new MijiaCore();

    public class Account {
        public ServiceLoginData serviceLogin() throws BusinessException {
            String serviceId = MiServer.mijia.getServiceId();
            UserTokenRedisDto userTokenRedisDto = (UserTokenRedisDto) redisHelper.vGet(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, xiaomiClientConfig.getAccount());
            if (userTokenRedisDto == null) {
                userTokenRedisDto = new UserTokenRedisDto();
                userTokenRedisDto.setServerTokenMap(new HashMap<>());
            }
            ServiceLoginData loginData;
            try {
                if (userTokenRedisDto.getUserId() == null) {
                    loginData = accountApiProxy.serviceLogin(xiaomiClientConfig.getAccount(), serviceId);
                } else {
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
            userTokenRedisDto.setEncryptedUserId(loginData.getEncryptedUserId());
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

    public class MijiaCore {
        private static final AtomicInteger id = new AtomicInteger();

        public List<DeviceData> deviceList() throws BusinessException {
            DeviceSearchParam deviceSearchParam = new DeviceSearchParam();
            deviceSearchParam.setGetVirtualModel(true);
            deviceSearchParam.setGetHuamiDevices(1);
            deviceSearchParam.setGetSplitDevice(true);
            deviceSearchParam.setSupportSmartHome(true);
            deviceSearchParam.setGetCariotDevice(true);
            deviceSearchParam.setGetThirdDevice(true);

            XiaomiRspV2<DeviceDataRsp> deviceList = mijiaCoreApiProxy.deviceList(deviceSearchParam);
            return deviceList.getResult().getList();
        }

        public Map<String, String> getDeviceStatus(String deviceId) throws BusinessException {
            DeviceParam deviceParam = new DeviceParam();
            deviceParam.setId(id.get());
            deviceParam.setMethod("get_prop");
            List<String> statusList = new ArrayList<>();
            statusList.add("ai_env");
            statusList.add("ai_provider");
            statusList.add("bright");
            statusList.add("microphone_mute");
            statusList.add("speaker_mute");
            statusList.add("speaker_rate");
            statusList.add("speaker_volume");

            deviceParam.setParams(statusList);

            XiaomiRspV2<List<String>> deviceList = mijiaApiProxy.getDeviceStatus("yeelink.wifispeaker.v1", deviceId, deviceParam);

            Map<String, String> resultMap = new TreeMap<>();
            for (int i = 0; i < statusList.size(); i++) {
                resultMap.put(statusList.get(i), deviceList.getResult().get(i));
            }
            return resultMap;
        }
    }

}
