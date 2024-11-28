package cn.jzyunqi.common.third.xiaomi.mijia.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wiiyaya
 * @since 2024/11/28
 */
@Getter
@Setter
@ToString
public class DeviceData {
    private String did;
    private String name;
    private String model;
    private String token;
    private Boolean isOnline;
}
