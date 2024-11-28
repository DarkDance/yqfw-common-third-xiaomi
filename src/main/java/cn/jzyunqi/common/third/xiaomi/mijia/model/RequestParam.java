package cn.jzyunqi.common.third.xiaomi.mijia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@Setter
@ToString
public class RequestParam {

    private String data;

    private String rc4Hash;

    private String signature;

    private String nonce;

    private String security;
}
