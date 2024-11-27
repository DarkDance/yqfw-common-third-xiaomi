package cn.jzyunqi.common.third.xiaomi.mijia.model;

import org.slf4j.event.KeyValuePair;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
public class NetRequest {
    public String prefix;
    public String path;
    public String method;
    public List<KeyValuePair> headers;
    public List<KeyValuePair> params;
}
