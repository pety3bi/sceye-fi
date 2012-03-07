/*
    Sceye-Fi Photo capture
    Copyright (C) 2011-2012  Maurice Perry <maurice@perry.ch>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tastefuljava.sceyefi.server;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumInputStream extends InputStream {
    private InputStream in;
    private int count;
    private int lobyte;
    private int sum;
    private MessageDigest digest;
    private boolean eof = false;

    public ChecksumInputStream(InputStream in) {
        try {
            this.in = in;
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public byte[] checksum(byte[] key) throws IOException {
        while (!eof) {
            read();
        }
        while (count != 0) {
            processByte(0);
        }
        return digest.digest(key);
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b < 0) {
            eof = true;
            return b;
        }
        processByte(b);
        return b;
    }

    private void processByte(int b) {
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
