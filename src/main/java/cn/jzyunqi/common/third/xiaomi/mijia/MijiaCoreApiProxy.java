package cn.jzyunqi.common.third.xiaomi.mijia;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange;
import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV2;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceChatParam;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceChatRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceDataRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceSearchParam;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceStatusParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 客服消息API
 *
 * @author wiiyaya
 * @since 2024/9/20
 */
@XiaomiHttpExchange
@HttpExchange(url = "https://{prefix}api.mijia.tech", contentType = "application/json")
public interface MijiaCoreApiProxy {

    //获取设备列表
    @PostExchange(url = "/app/v2/home/device_list_page")
    XiaomiRspV2<DeviceDataRsp> deviceList(@PathVariable String prefix, @RequestBody DeviceSearchParam deviceSearchParam) throws BusinessException;

    //获取设备状态
    @PostExchange(url = "/app/home/rpc/{deviceId}")
    XiaomiRspV2<List<String>> executeDeviceMethod(@PathVariable String prefix, @RequestHeader("miot-request-model") String model, @PathVariable String deviceId, @RequestBody DeviceStatusParam deviceStatusParam) throws BusinessException;

    //获取设备对话列表
    @PostExchange(url = "/app/v2/api/aivs")
    XiaomiRspV2<DeviceChatRsp> getDeviceChatList(@PathVariable String prefix, @RequestHeader("miot-request-model") String model, @RequestBody DeviceChatParam deviceChatParam) throws BusinessException;
}
