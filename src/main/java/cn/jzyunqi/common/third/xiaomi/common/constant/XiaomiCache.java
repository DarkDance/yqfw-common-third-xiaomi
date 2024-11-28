package cn.jzyunqi.common.third.xiaomi.common.constant;

import cn.jzyunqi.common.feature.redis.Cache;
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
    THIRD_XIAOMI_ACCOUNT_V(Duration.ZERO, Boolean.FALSE),

    ;
    private final Duration expiration;
    private final Boolean autoRenew;
}
