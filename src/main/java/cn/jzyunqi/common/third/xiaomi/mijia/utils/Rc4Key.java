package cn.jzyunqi.common.third.xiaomi.mijia.utils;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
public class Rc4Key {

    private int _i;

    private int _j;

    private final byte[] key = new byte[256];

    public Rc4Key(byte[] rc4Key) {
        int length = rc4Key.length;
        for (int i = 0; i < 256; i++) {
            this.key[i] = (byte) i;
        }
        int i = 0;
        for (int j = 0; j < 256; j++) {
            byte byteJ = this.key[j];
            i = (i + rc4Key[j % length] + byteJ) & 255;
            this.key[j] = this.key[i];
            this.key[i] = byteJ;
        }
        this._i = 0;
        this._j = 0;
    }

    public final void compute(byte[] needCompute) {
        for (int i = 0; i < needCompute.length; i++) {
            int j = (this._i + 1) & 255;
            this._i = j;
            byte byteJ = this.key[j];
            int k = (this._j + byteJ) & 255;
            this._j = k;
            this.key[j] = this.key[k];
            this.key[k] = byteJ;
            needCompute[i] = (byte) (needCompute[i] ^ this.key[(this.key[j] + byteJ) & 255]);
        }
    }
}
