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
import cn.jzyunqi.common.third.xiaomi.mijia.enums.YeelightProp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceChatData;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceChatParam;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceChatRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceData;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceDataRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceSearchParam;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceStatusParam;
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
    private MijiaApiProxy mijiaCoreApiProxy;

    @Resource
    private AccountApiProxy accountApiProxy;

    @Resource
    private XiaomiAuthRepository xiaomiAuthRepository;

    @Resource
    private RedisHelper redisHelper;

    public XiaomiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public final Account account = new Account();

    public final MijiaApi mijiaApi = new MijiaApi();

    public class Account {
        /**
         * 登录小米账号
         *
         * @param xiaomiAccount 小米账号
         * @return 登录结果
         */
        public ServiceLoginData serviceLogin(String xiaomiAccount) throws BusinessException {
            XiaomiAuth auth = xiaomiAuthRepository.getXiaomiAuth(xiaomiAccount);

            String serviceId = MiServer.mijia.getServiceId();
            UserTokenRedisDto userTokenRedisDto = (UserTokenRedisDto) redisHelper.vGet(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, auth.getAccount());
            if (userTokenRedisDto == null) {
                userTokenRedisDto = new UserTokenRedisDto();
                userTokenRedisDto.setServerTokenMap(new HashMap<>());
            }
            ServiceLoginData loginData;
            try {
                if (userTokenRedisDto.getUserId() == null) {
                    loginData = accountApiProxy.serviceLogin(auth.getAccount(), serviceId);
                } else {
                    loginData = accountApiProxy.serviceLogin(userTokenRedisDto.getUserId(), userTokenRedisDto.getPassToken(), serviceId);
                }
            } catch (BusinessException e) {
                ServiceLoginData errorData = (ServiceLoginData) e.getArguments()[2];
                loginData = accountApiProxy.serviceLoginAuth2(
                        errorData.getQueryStr(),
                        errorData.getCallback(),
                        errorData.getSign(),
                        auth.getAccount(),
                        DigestUtilPlus.MD5.sign(auth.getPassword(), false).toUpperCase()
                );
            }
            userTokenRedisDto.setUserId(loginData.getUserId());
            userTokenRedisDto.setEncryptedUserId(loginData.getEncryptedUserId());
            userTokenRedisDto.setPassToken(loginData.getPassToken());
            redisHelper.vPut(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, auth.getAccount(), userTokenRedisDto);

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
                                redisHelper.vPut(XiaomiCache.THIRD_XIAOMI_ACCOUNT_V, auth.getAccount(), finalUserTokenRedisDto);
                            });
                    ;
                }
            }
            return loginData;
        }
    }

    public class MijiaApi {
        private static final AtomicInteger id = new AtomicInteger();

        /**
         * 获取指定账号下的设备列表（需要先登录后才能获取）
         *
         * @param xiaomiAccount 小米账号
         * @return 设备列表
         */
        public List<DeviceData> deviceList(String xiaomiAccount) throws BusinessException {
            DeviceSearchParam deviceSearchParam = new DeviceSearchParam();
            deviceSearchParam.setGetVirtualModel(true);
            deviceSearchParam.setGetHuamiDevices(1);
            deviceSearchParam.setGetSplitDevice(true);
            deviceSearchParam.setSupportSmartHome(true);
            deviceSearchParam.setGetCariotDevice(true);
            deviceSearchParam.setGetThirdDevice(true);

            XiaomiRspV2<DeviceDataRsp> deviceList = mijiaCoreApiProxy.deviceList(xiaomiAccount, deviceSearchParam);
            return deviceList.getResult().getList();
        }

        /**
         * 获取设备各种属性
         *
         * @param xiaomiAccount 小米账号
         * @param deviceId 设备id（从设备列表中获取）
         * @param deviceModel 设备模型（从设备列表中获取）
         * @return 设备当前各种状态
         */
        public Map<String, String> getDeviceStatus(String xiaomiAccount, String deviceId, String deviceModel) throws BusinessException {
            DeviceStatusParam deviceParam = new DeviceStatusParam();
            deviceParam.setId(id.get());
            deviceParam.setMethod("get_prop");
            List<Object> statusList = new ArrayList<>();
            statusList.add("ai_env");
            statusList.add("ai_provider");
            statusList.add("bright");
            statusList.add("microphone_mute");
            statusList.add("speaker_mute");
            statusList.add("speaker_rate");
            statusList.add("speaker_volume");

            deviceParam.setParams(statusList);

            XiaomiRspV2<List<String>> deviceList = mijiaCoreApiProxy.executeDeviceMethod(xiaomiAccount, deviceModel, deviceId, deviceParam);

            Map<String, String> resultMap = new TreeMap<>();
            for (int i = 0; i < statusList.size(); i++) {
                resultMap.put(statusList.get(i).toString(), deviceList.getResult().get(i));
            }
            return resultMap;
        }

        /**
         * 设置设备属性
         *
         * @param xiaomiAccount 小米账号
         * @param deviceId 设备id（从设备列表中获取）
         * @param deviceModel 设备模型（从设备列表中获取）
         * @param prop 属性
         * @param value 属性值
         * @return 设置结果
         */
        public String setDeviceStatus(String xiaomiAccount, String deviceId, String deviceModel, YeelightProp prop, String value) throws BusinessException {
            DeviceStatusParam deviceParam = new DeviceStatusParam();
            deviceParam.setId(id.get());
            deviceParam.setMethod(prop.toString());
            if (StringUtilPlus.isNotBlank(value)) {
                if (prop.getParamType() == Boolean.class) {
                    deviceParam.setParams(List.of(Boolean.valueOf(value)));
                } else if (prop.getParamType() == Integer.class) {
                    deviceParam.setParams(List.of(Integer.valueOf(value)));
                } else {
                    deviceParam.setParams(List.of(value));
                }
            }
            XiaomiRspV2<List<String>> deviceList = mijiaCoreApiProxy.executeDeviceMethod(xiaomiAccount, deviceModel, deviceId, deviceParam);
            return deviceList.getResult().get(0);
        }

        /**
         * 与设备聊天
         *
         * @param xiaomiAccount 小米账号
         * @param deviceId 设备id（从设备列表中获取）
         * @param deviceModel 设备模型（从设备列表中获取）
         * @param chatContent 聊天内容
         * @return 方法调用结果
         */
        public String chatWithDevice(String xiaomiAccount, String deviceId, String deviceModel, String chatContent) throws BusinessException {
            DeviceStatusParam deviceParam = new DeviceStatusParam();
            deviceParam.setId(id.get());
            deviceParam.setMethod("start_user_nlp");
            List<Object> statusList = new ArrayList<>();
            statusList.add(chatContent);
            statusList.add(0);

            deviceParam.setParams(statusList);

            XiaomiRspV2<List<String>> deviceList = mijiaCoreApiProxy.executeDeviceMethod(xiaomiAccount, deviceModel, deviceId, deviceParam);
            return deviceList.getResult().get(0);
        }

        /**
         * 让设备说话
         *
         * @param xiaomiAccount 小米账号
         * @param deviceId 设备id（从设备列表中获取）
         * @param deviceModel 设备模型（从设备列表中获取）
         * @param ttsContent 说话内容
         * @return 方法调用结果
         */
        public String ttsWithDevice(String xiaomiAccount, String deviceId, String deviceModel, String ttsContent) throws BusinessException {
            DeviceStatusParam deviceParam = new DeviceStatusParam();
            deviceParam.setId(id.get());
            deviceParam.setMethod("play_user_tts");
            List<Object> statusList = new ArrayList<>();
            statusList.add(ttsContent);
            statusList.add(1);
            statusList.add(1800);

            deviceParam.setParams(statusList);

            XiaomiRspV2<List<String>> deviceList = mijiaCoreApiProxy.executeDeviceMethod(xiaomiAccount, deviceModel, deviceId, deviceParam);
            return deviceList.getResult().get(0);
        }

        /**
         * 获取设备聊天记录
         *
         * @param xiaomiAccount 小米账号
         * @param userId 用户id 登陆后获取
         * @param deviceId 设备id（从设备列表中获取）
         * @param deviceModel 设备模型（从设备列表中获取）
         * @param clientId 暂不知道怎么来的
         * @return 聊天记录
         */
        public DeviceChatData getDeviceChatList(String xiaomiAccount, String userId, String deviceId, String deviceModel, String clientId) throws BusinessException {
            DeviceChatParam deviceChatParam = new DeviceChatParam();
            deviceChatParam.setPath("/api/aivs/device-events");
            deviceChatParam.setMethod("POST");
            deviceChatParam.setEnv(0);

            DeviceChatParam.DefaultParams defaultParams = new DeviceChatParam.DefaultParams();
            defaultParams.setUserId(userId);
            defaultParams.setModel(deviceModel);
            defaultParams.setDid(deviceId);
            defaultParams.setClientId(clientId);
            deviceChatParam.setParams(defaultParams);

            DeviceChatParam.SpecialParams specialParams = new DeviceChatParam.SpecialParams();
            specialParams.setStart(System.currentTimeMillis());
            specialParams.setPageSize(2);
            deviceChatParam.setPayload(specialParams);

            DeviceChatParam.DefaultHeader defaultHeader = new DeviceChatParam.DefaultHeader();
            defaultHeader.setName("DialogRecord.FetchByTime");
            deviceChatParam.setHeader(defaultHeader);

            DeviceChatParam.SpecialHeader specialHeader = new DeviceChatParam.SpecialHeader();
            specialHeader.setContentType(List.of("application/json"));
            deviceChatParam.setReqHeader(specialHeader);

            XiaomiRspV2<DeviceChatRsp> deviceList = mijiaCoreApiProxy.getDeviceChatList(xiaomiAccount, deviceModel, deviceChatParam);
            return deviceList.getResult().getRet();
        }
    }
}
