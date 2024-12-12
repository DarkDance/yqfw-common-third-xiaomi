package cn.jzyunqi.common.third.xiaomi.mijia;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange;
import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV2;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceDataRsp;
import cn.jzyunqi.common.third.xiaomi.mijia.model.DeviceSearchParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 客服消息API
 *
 * @author wiiyaya
 * @since 2024/9/20
 */
@XiaomiHttpExchange
@HttpExchange(url = "https://core.api.mijia.tech", contentType = "application/json")
public interface MijiaCoreApiProxy {

    //获取设备列表
    @PostExchange(url = "/app/v2/home/device_list_page")
    XiaomiRspV2<DeviceDataRsp> deviceList(@RequestBody DeviceSearchParam deviceSearchParam) throws BusinessException;
}
