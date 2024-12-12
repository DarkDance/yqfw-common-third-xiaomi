package cn.jzyunqi.common.third.xiaomi.mijia.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/12/10
 */
@Getter
@Setter
@ToString
public class DeviceParam {
    private Integer id;
    private String method;
    private List<String> params;
}
