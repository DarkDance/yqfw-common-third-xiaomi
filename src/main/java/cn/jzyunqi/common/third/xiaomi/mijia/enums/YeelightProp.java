package cn.jzyunqi.common.third.xiaomi.mijia.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wiiyaya
 * @since 2024/12/12
 *
 * https://home.miot-spec.com/spec/yeelink.wifispeaker.v1
 */
@Getter
@AllArgsConstructor
public enum YeelightProp {
    //get_prop, //获取全部状态

    set_player(String.class), //播放器 toggle/pause/resume
    set_virtual_btn(String.class), // 虚拟按键  null
    set_bright(Integer.class), //指示灯亮度  0-100
    set_microphone_mute(Boolean.class), // 麦克风静音  0-1
    set_speaker_volume(Integer.class), // 扬声器音量  0-100
    set_speaker_mute(Boolean.class), // 扬声器静音  0-1

    stop_alarm(String.class), // 停止闹钟  null
    switch_ai(String.class), // 切换ai  null/xiaobing/mibrain 小爱

    //start_user_nlp, // 开始对话
    //play_user_tts, // TTS
;
    private final Class<?> paramType;
}
