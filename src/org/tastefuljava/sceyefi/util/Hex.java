package org.tastefuljava.sceyefi.util;

public class Hex {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static byte[] hex2bin(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string with odd length");
        }
        char[] chars = hex.toCharArray();
        byte[] result = new byte[chars.length/2];
        int j = 0;
        for (int i = 0; i < result.length; ++i) {
            int val = (charValue(chars[j++])*16 + charValue(chars[j++]));
            result[i] = (byte)val;
        }
        return result;
    }

    public static String bin2hex(byte[] data) {
        return bin2hex(data, 0, data.length);
    }

    public static String bin2hex(byte[] data, int offs, int len) {
        int end = offs + len;
        char[] chars = new char[2*len];
        int j = 0;
        for (int i = offs; i < end; ++i) {
            int b = data[i] & 0xFF;
            chars[j++] = HEX_CHARS[b/16];
            chars[j++] = HEX_CHARS[b%16];
        }
        return new String(chars);
    }

    private static int charValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else {
            throw new IllegalArgumentException("Illegal hex char " + c);
        }
    }
}
