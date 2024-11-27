package cn.jzyunqi.common.third.xiaomi.account;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.third.xiaomi.account.model.ServiceLoginData;
import cn.jzyunqi.common.third.xiaomi.common.XiaomiHttpExchange;
import org.springframework.web.bind.annotation.CookieValue;
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
    @GetExchange(url = "/pass/serviceLogin?_json=true&sid=mijia&_locale=zh_CN")
    ServiceLoginData serviceLogin(@CookieValue("userId") String userId) throws BusinessException;

    //登录
    @PostExchange(url = "/pass/serviceLoginAuth2?_json=true&sid=mijia&_locale=zh_CN")
    ServiceLoginData serviceLoginAuth2(@RequestParam("qs") String queryStr,
                                       @RequestParam("callback") String callback,
                                       @RequestParam("_sign") String sign,
                                       @RequestParam("user") String user,
                                       @RequestParam("hash") String passwordHash
    ) throws BusinessException;
}
