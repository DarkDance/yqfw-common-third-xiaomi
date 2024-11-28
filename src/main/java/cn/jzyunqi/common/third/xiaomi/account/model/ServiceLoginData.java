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
    private String serverSecurity;//如果没有，就在头上Extension-Pragma

    private String passToken;

    private Long nonce;//如果没有，就在头上Extension-Pragma

    private Long userId;

    @JsonProperty("cUserId")
    private String encryptedUserId;

    @JsonProperty("psecurity")
    private String passSecurity;//如果没有，就在头上Extension-Pragma

    @JsonProperty("qs")
    private String queryStr;

    private String location;

    @JsonProperty("pwd")
    private Integer hasPassword;

    @JsonProperty("child")
    private Integer isChild;

    private String captchaUrl;

    private Integer securityStatus;
}

