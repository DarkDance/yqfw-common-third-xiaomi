package cn.jzyunqi.common.third.xiaomi.mijia.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/11/28
 */
@Getter
@Setter
@ToString
public class DeviceDataRsp {
    private List<DeviceData> list;
}
