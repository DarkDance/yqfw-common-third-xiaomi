package cn.jzyunqi.common.third.xiaomi.mijia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DeviceSearchParam {

    private Boolean getVirtualModel;

    private Integer getHuamiDevices;

    @JsonProperty("get_split_device")
    private Boolean getSplitDevice;

    @JsonProperty("support_smart_home")
    private Boolean supportSmartHome;

    @JsonProperty("get_cariot_device")
    private Boolean getCariotDevice;

    @JsonProperty("get_third_device")
    private Boolean getThirdDevice;
}
