package cn.jzyunqi.common.third.xiaomi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author wiiyaya
 * @since 2025/2/5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XiaomiAuth {

    /**
     * 米家账号
     */
    private String account;

    /**
     * 米家密码
     */
    private String password;
}
