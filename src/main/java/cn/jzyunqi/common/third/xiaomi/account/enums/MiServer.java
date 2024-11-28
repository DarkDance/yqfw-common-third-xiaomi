package cn.jzyunqi.common.third.xiaomi.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wiiyaya
 * @since 2024/11/28
 */
@Getter
@AllArgsConstructor
public enum MiServer {
    mijia("mijia", "https://account.xiaomi.com"),
    xiaoqiang("xiaoqiang", "https://api.miwifi.com/sts"),
    xiaomihome("xiaomihome", "https://home.mi.com/sts"),
    tsm_auth("tsm-auth", "https://tsmapi.pay.xiaomi.com/sts"),
    passportapi("passportapi", "https://api.account.xiaomi.com/sts"),
    miotstore("miotstore", "https://shopapi.io.mi.com/app/shop/auth"),
    mi_huodong("mi_huodong", "https://i.huodong.mi.com/login/callback"),
    mi_eshopm_go("mi_eshopm_go", "https://m.mi.com/v1/authorize/sso_callback"),
    kfs_chat("kfs_chat", "https://chat.kefu.mi.com/sts"),
    i_ai_mi_com("i.ai.mi.com", "https://i.ai.mi.com/sts"),
    ;
    private final String serviceId;
    private final String urlRemark;
}
