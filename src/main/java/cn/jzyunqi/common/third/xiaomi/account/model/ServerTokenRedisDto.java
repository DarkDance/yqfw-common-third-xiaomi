package cn.jzyunqi.common.third.xiaomi.account.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wiiyaya
 * @since 2024/11/27
 */
@Getter
@Setter
public class ServerTokenRedisDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -913059388587855126L;

    private String serverId;
    private String serverSecurity;
    private String serverToken;
}
