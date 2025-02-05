package cn.jzyunqi.common.third.xiaomi;

import cn.jzyunqi.common.utils.StringUtilPlus;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/9/23
 */
public interface XiaomiAuthRepository {

    List<XiaomiAuth> getXiaomiAuthList();

    default XiaomiAuth getXiaomiAuth(String account) {
        if(StringUtilPlus.isEmpty(account)){
            return getXiaomiAuthList().stream().findFirst().orElse(new XiaomiAuth());
        }else{
            return getXiaomiAuthList().stream().filter(authInfo -> authInfo.getAccount().equals(account)).findFirst().orElse(new XiaomiAuth());
        }
    }
}
