package cn.jzyunqi.common.third.xiaomi.common;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.common.model.XiaomiRspV1;
import cn.jzyunqi.common.utils.StringUtilPlus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * @author wiiyaya
 * @since 2024/9/23
 */
@Slf4j
@Aspect
@Order
public class XiaomiHttpExchangeWrapper {

    /**
     * 所有标记了@xiaomiHttpExchange的类下所有的方法
     */
    @Pointcut("within(@cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange *)")
    public void xiaomiHttpExchange() {
    }

    @Around(value = "xiaomiHttpExchange() ", argNames = "proceedingJoinPoint")
    public Object Around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.debug("======xiaomiHttpExchange[{}] start=======", proceedingJoinPoint.getSignature().getName());
        Object resultObj;
        try {
            resultObj = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            log.debug("======xiaomiHttpExchange[{}] internal exception=======", proceedingJoinPoint.getSignature().getName());
            throw new BusinessException("common_error_xiaomi_http_exchange_error", e);
        }
        if (resultObj instanceof XiaomiRspV1 xiaomiRsp) {
            if (StringUtilPlus.isNotBlank(xiaomiRsp.getCode()) && !"0".equals(xiaomiRsp.getCode())) {
                log.debug("======xiaomiHttpExchange[{}] response failed=======", proceedingJoinPoint.getSignature().getName());
                throw new BusinessException("common_error_xiaomi_http_exchange_failed", xiaomiRsp.getCode(), xiaomiRsp.getDescription(), xiaomiRsp);
            }
        }
        log.debug("======xiaomiHttpExchange[{}] success=======", proceedingJoinPoint.getSignature().getName());
        return resultObj;
    }
}
