/**
 * This file is a part of sunlight project Copyright (c) $today.year sunlight authors (see file
 * `COPYRIGHT` for the license)
 */
package com.ghisguth.shared;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ResourceHelper {
    public static String loadRawString(InputStream stream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toString();
    }
}
