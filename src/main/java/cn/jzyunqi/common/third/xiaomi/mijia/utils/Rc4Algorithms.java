package cn.jzyunqi.common.third.xiaomi.mijia.utils;

import cn.jzyunqi.common.utils.DigestUtilPlus;
import cn.jzyunqi.common.utils.IOUtilPlus;
import cn.jzyunqi.common.utils.StringUtilPlus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author wiiyaya
 * @since 2024/11/26
 */
public class Rc4Algorithms {
    public static final byte[] emptyBytes;
    public final Rc4Key rc4Key;

    static {
        emptyBytes = new byte[1024];
        Arrays.fill(emptyBytes, (byte) 0);
    }

    public Rc4Algorithms(String rc4Key) throws SecurityException {
        this(DigestUtilPlus.Base64.decodeBase64(rc4Key));
    }

    public Rc4Algorithms(byte[] rc4KeyByte) throws SecurityException {
        if (rc4KeyByte == null || rc4KeyByte.length == 0) {
            throw new SecurityException("rc4 key is null");
        }
        if (rc4KeyByte.length != 32) {
            throw new IllegalArgumentException("rc4Key length is invalid");
        }
        this.rc4Key = new Rc4Key(rc4KeyByte);
        this.rc4Key.compute(emptyBytes);
    }

    public final String decrypt(String base64Str, boolean gzipFormat) {
        byte[] decryptBytes = DigestUtilPlus.Base64.decodeBase64(base64Str);
        this.rc4Key.compute(decryptBytes);
        if (!gzipFormat) {
            return new String(decryptBytes, StandardCharsets.UTF_8);
        }
        try (InputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decryptBytes))) {
            return IOUtilPlus.toString(gzipInputStream, StringUtilPlus.UTF_8);
        } catch (IOException e) {
            return base64Str;
        }
    }

    public final String encrypt(String content) {
        byte[] contentBytes = content.getBytes(StringUtilPlus.UTF_8);
        this.rc4Key.compute(contentBytes);
        return DigestUtilPlus.Base64.encodeBase64String(contentBytes);
    }
}
