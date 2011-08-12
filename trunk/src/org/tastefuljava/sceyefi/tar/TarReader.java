package org.tastefuljava.sceyefi.tar;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class TarReader {
    private static final int BLOCK_SIZE = 512;

    private InputStream in;
    private byte[] buffer = new byte[BLOCK_SIZE];

    public TarReader(InputStream in) throws IOException {
        this.in = in;
    }

    public void close() throws IOException {
        in.close();
    }

    private void readBuffer() throws IOException {
        int n = in.read(buffer);
        if (n != BLOCK_SIZE) {
            throw new IOException("Invalid tar file");
        }
    }

    public TarEntry nextEntry() throws IOException {
        readBuffer();
        if (buffer[0] == 0) {
            return null;
        }
        String fileName = getHeaderField(0, 100);
        final long length = Long.parseLong(getHeaderField(124, 12), 8);
        long lastModified = Long.parseLong(getHeaderField(136, 12), 8)*1000L;
        return new TarEntry(fileName, new Date(lastModified), length,
                new InputStream() {
            private long pos;
            private int index;

            @Override
            public int read() throws IOException {
                if (pos >= length) {
                    return -1;
                }
                if (index == 0) {
                    readBuffer();
                }
                int result = buffer[index++] & 0xFF;
                if (index == BLOCK_SIZE) {
                    index = 0;
                }
                ++pos;
                return result;
            }

            @Override
            public void close() throws IOException {
                if (pos < length) {
                    pos -= pos % BLOCK_SIZE;
                    do {
                        readBuffer();
                        pos += BLOCK_SIZE;
                    } while (pos < length);
                }
            }
        });
    }

    private String getHeaderField(int offs, int len)
            throws UnsupportedEncodingException {
        int end = offs + len;
        int i = offs;
        while (i < end && buffer[i] != 0) {
            ++i;
        }
        return new String(buffer, offs, i-offs, "ASCII");
    }
}
