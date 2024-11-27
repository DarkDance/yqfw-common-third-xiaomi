package cn.jzyunqi.common.third.xiaomi.mijia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@Setter
public class RequestParam {

    @JsonIgnore
    private Map<String, String> requestParams;

    private String data;

    @JsonProperty("rc4_hash__")
    private String rc4Hash;

    private String signature;

    @JsonProperty("_nonce")
    private String nonce;

    @JsonProperty("ssecurity")
    private String security;
}
