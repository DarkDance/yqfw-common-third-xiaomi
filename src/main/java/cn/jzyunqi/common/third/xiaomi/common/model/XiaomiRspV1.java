package cn.jzyunqi.common.third.xiaomi.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wiiyaya
 * @since 2018/5/22.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class XiaomiRspV1 {
    /**
     * 结果代码
     */
    private String code;

    /**
     * 结果
     */
    private String result;

    /**
     * 结果描述
     */
    private String description;

    /**
     * 结果描述
     */
    private String desc;

    @JsonProperty("qs")
    private String queryStr;

    private String location;

    @JsonProperty("pwd")
    private Integer password;

    private Integer child;

    private String captchaUrl;

    private Integer securityStatus;
}
