package cn.jzyunqi.common.third.xiaomi.account.model;

import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV1;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ServiceLoginData extends XiaomiRspV1 {

    private String serviceParam;

    @JsonProperty("_sign")
    private String sign;

    @JsonProperty("sid")
    private String serviceId;

    private String callback;

    @JsonProperty("ssecurity")
    private String serverSecurity;

    private String passToken;

    private Long nonce;

    private Long userId;

    @JsonProperty("cUserId")
    private String clientUserId;

    @JsonProperty("psecurity")
    private String passSecurity;
}

