package cn.jzyunqi.common.third.xiaomi.mijia.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author wiiyaya
 * @since 2024/12/12
 */
@Getter
@Setter
@ToString
public class DeviceChatData {

    private String clientId;
    private String deviceId;
    private String userId;
    private List<ChatRecord> records;

    @Getter
    @Setter
    @ToString
    public static class ChatRecord {
        private String content;
        private String speaker;
        private Long time;
        private List<String> widget;
        private String widgetType;
    }
}
