package cn.jzyunqi.common.third.xiaomi.account.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@Setter
public class UserTokenRedisDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 2348214333759279461L;

    private Long userId;
    private String passToken;
    private Map<String, ServerTokenRedisDto> serverTokenMap;
}
