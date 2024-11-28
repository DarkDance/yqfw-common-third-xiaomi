package cn.jzyunqi.common.third.xiaomi.account;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.account.model.ServiceLoginData;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 账户相关API
 *
 * @author wiiyaya
 * @since 2024/9/20
 */
@XiaomiHttpExchange
@HttpExchange(url = "https://account.xiaomi.com", contentType = "application/x-www-form-urlencoded")
public interface AccountApiProxy {

    //登录检查
    @GetExchange(url = "/pass/serviceLogin?_json=true&sid={serviceId}&_locale=zh_CN")
    ServiceLoginData serviceLogin(@CookieValue String userId, @PathVariable String serviceId) throws BusinessException;

    //登录检查
    @GetExchange(url = "/pass/serviceLogin?_json=true&sid={serviceId}&_locale=zh_CN")
    ServiceLoginData serviceLogin(@CookieValue Long userId, @CookieValue String passToken, @PathVariable String serviceId) throws BusinessException;

    //登录
    @PostExchange(url = "/pass/serviceLoginAuth2?_json=true&sid=mijia&_locale=zh_CN")
    ServiceLoginData serviceLoginAuth2(@RequestParam String qs,
                                       @RequestParam String callback,
                                       @RequestParam("_sign") String sign,
                                       @RequestParam String user,
                                       @RequestParam("hash") String passwordHash
    ) throws BusinessException;
}
