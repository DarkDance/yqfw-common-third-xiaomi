package cn.jzyunqi.common.third.xiaomi.common.constant;

import cn.jzyunqi.common.support.spring.redis.Cache;
import cn.jzyunqi.common.third.xiaomi.account.model.UserTokenRedisDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@AllArgsConstructor
public enum XiaomiCache implements Cache {
    THIRD_XIAOMI_ACCOUNT_V(Duration.ZERO, Boolean.FALSE, UserTokenRedisDto.class),

    ;
    private final Duration expiration;
    private final Boolean autoRenew;
    private final Object valueType;
}
