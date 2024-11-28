package cn.jzyunqi.common.third.xiaomi.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wiiyaya
 * @since 2024/11/28
 */
@Getter
@Setter
@ToString
public class XiaomiRspV2<T> {
    /**
     * 结果代码
     */
    private String code;

    /**
     * 结果描述
     */
    private String message;

    /**
     * 数据
     */
    private T result;
}
