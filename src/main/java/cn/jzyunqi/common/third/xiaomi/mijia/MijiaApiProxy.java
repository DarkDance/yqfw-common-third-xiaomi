package cn.jzyunqi.common.third.xiaomi.mijia;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange;
import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV2;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceParam;
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
@HttpExchange(url = "https://api.mijia.tech", contentType = "application/json")
public interface MijiaApiProxy {

    //获取设备状态
    @PostExchange(url = "/app/home/rpc/{deviceId}")
    XiaomiRspV2<List<String>> getDeviceStatus(@RequestHeader("miot-request-model") String model, @PathVariable String deviceId, @RequestBody DeviceParam deviceParam) throws BusinessException;
}
