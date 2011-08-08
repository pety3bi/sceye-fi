package org.tastefuljava.sceyefi.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumCalculator {
    private int count;
    private int lobyte;
    private int sum;
    private MessageDigest digest;

    public ChecksumCalculator() {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public byte[] checksum(byte[] key) {
        while (count != 0) {
            processByte(0);
        }
        return digest.digest(key);
    }

    public void processBytes(byte[] data, int offs, int len) {
        int end = offs + len;
        for (int i = offs; i < end; ++i) {
            processByte(data[i] & 0xFF);
        }
    }

    public void processByte(int b) {
        ++count;
        if (count%2 != 0) {
            lobyte = b;
        } else {
            sum += lobyte | (b << 8);
            if (count == 512) {
                int hiword = sum >>> 16;
                while (hiword != 0) {
                    sum = (sum & 0xFFFF) + hiword;
                    hiword = sum >>> 16;
                }
                sum ^= 0xFFFF;
                digest.update((byte)(sum & 0xFF));
                digest.update((byte)(sum >>> 8));
                sum = 0;
                count = 0;
            }
        }
    }
}
