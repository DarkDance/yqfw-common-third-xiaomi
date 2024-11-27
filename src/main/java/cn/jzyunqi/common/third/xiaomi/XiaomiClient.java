package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.exception.BusinessException;
import cn.jzyunqi.common.feature.redis.RedisHelper;
import cn.jzyunqi.common.third.xiaomi.account.AccountApiProxy;
import cn.jzyunqi.common.third.xiaomi.account.model.ServiceLoginData;
import cn.jzyunqi.common.utils.DigestUtilPlus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import static org.bouncycastle.asn1.cms.CMSObjectIdentifiers.data;

/**
 * 公众号客户端
 *
 * @author wiiyaya
 * @since 2024/9/23
 */
@Slf4j
public class XiaomiClient {

    @Resource
    private AccountApiProxy accountApiProxy;

    @Resource
    private XiaomiClientConfig xiaomiClientConfig;

    @Resource
    private RedisHelper redisHelper;

    public final Account account = new Account();

    public class Account {
        public ServiceLoginData serviceLogin() throws BusinessException {
            ServiceLoginData data;
            try {
                data = accountApiProxy.serviceLogin(xiaomiClientConfig.getAccount());
            } catch (BusinessException e) {
                ServiceLoginData errorData = (ServiceLoginData) e.getArguments()[2];
                data = accountApiProxy.serviceLoginAuth2(
                        errorData.getQueryStr(),
                        errorData.getCallback(),
                        errorData.getSign(),
                        xiaomiClientConfig.getAccount(),
                        DigestUtilPlus.MD5.sign(xiaomiClientConfig.getPassword(), false).toUpperCase()
                );
            }
            return data;
        }
    }

}
