/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    private static final int BUFFER_SIZE = 8192;

    @NotNull
    public static byte[] toByte(@NotNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE * 4);
        byte[] bs = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        inputStream.close();
        return os.toByteArray();
    }

    public static int toSize(@NotNull FileInputStream inputStream) throws IOException {
        return inputStream.available();
    }

    @NotNull
    public static byte[] ReadZipEntry(@NotNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE * 4);
        byte[] bs = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        return os.toByteArray();
    }
}
