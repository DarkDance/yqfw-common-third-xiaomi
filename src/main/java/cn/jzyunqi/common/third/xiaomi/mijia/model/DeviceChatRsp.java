package cn.jzyunqi.common.third.xiaomi.mijia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author wiiyaya
 * @since 2024/12/12
 */
@Getter
@Setter
@ToString
public class DeviceChatRsp {
    @JsonProperty("resp_code")
    private String respCode;
    @JsonProperty("resp_header")
    private Map<String, Object> respHeader;
    private DeviceChatData ret;
}
