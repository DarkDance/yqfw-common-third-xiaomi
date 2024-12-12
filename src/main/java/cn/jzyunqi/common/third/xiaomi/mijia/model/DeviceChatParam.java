package cn.jzyunqi.common.third.xiaomi.mijia.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/12/12
 */
@Getter
@Setter
@ToString
public class DeviceChatParam {
    @JsonProperty("req_method")
    private String method;
    private String path;
    private Integer env;

    private DefaultHeader header;
    @JsonProperty("req_header")
    private SpecialHeader reqHeader;

    private DefaultParams params;
    private SpecialParams payload;


    @Getter
    @Setter
    @ToString
    public static class DefaultParams {
        @JsonProperty("user_id")
        private String userId;
        private String model;
        private String did;
        @JsonProperty("client_id")
        private String clientId;
    }

    @Getter
    @Setter
    @ToString
    public static class DefaultHeader {
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class SpecialParams {
        private Long start;
        @JsonProperty("pagesize")
        private Integer pageSize;
    }

    @Getter
    @Setter
    @ToString
    public static class SpecialHeader {
        @JsonProperty("Content-Type")
        private List<String> contentType;
    }
}
