package cn.jzyunqi.common.third.xiaomi.mijia.enums;

/**
 * @author wiiyaya
 * @since 2024/12/12
 *
 * https://home.miot-spec.com/spec/yeelink.wifispeaker.v1
 */
public enum YeelightProp {
    //get_prop, //获取全部状态

    set_player, //播放器 toggle/pause/resume
    set_virtual_btn, // 虚拟按键  null
    set_bright, //指示灯亮度  0-100
    set_microphone_mute, // 麦克风静音  0-1
    set_speaker_volume, // 扬声器音量  0-100
    set_speaker_mute, // 扬声器静音  0-1

    stop_alarm, // 停止闹钟  null
    switch_ai, // 切换ai  null/xiaobing/mibrain 小爱

    //start_user_nlp, // 开始对话
    //play_user_tts, // TTS

}
