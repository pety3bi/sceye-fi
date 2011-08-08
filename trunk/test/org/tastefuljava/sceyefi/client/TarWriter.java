package org.tastefuljava.sceyefi.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class TarWriter extends OutputStream {
    private static final int BLOCK_SIZE = 512;

    private OutputStream out;
    private long position;
    private byte[] buffer = new byte[BLOCK_SIZE];
    private long entrySize;

    public TarWriter(OutputStream out) {
        this.out = out;
    }

    public void startEntry(String fileName, long size, Date timestamp)
            throws IOException {
        entrySize = size;
        position = 0;
        for (int i = 0; i < BLOCK_SIZE; ++i) {
            buffer[i] = 0;
        }
        setField(0, 100, fileName);
        setField(124, 12, Long.toOctalString(size));
        setField(136, 12, Long.toOctalString(timestamp.getTime()/1000L));
        for (int i = 0; i < 8; ++i) {
            buffer[148+i] = ' ';
        }
        int checksum = 0;
        for (int i = 0; i < BLOCK_SIZE; ++i) {
            checksum += buffer[i] & 0xFF;
        }
        setField(148, 6, Long.toOctalString(checksum));
        out.write(buffer);
    }

    public void endEntry() throws IOException {
        if (position != entrySize) {
            throw new IOException("Invalid entry size");
        }
        int pad = (int)(position%BLOCK_SIZE);
        if (pad > 0) {
            pad = BLOCK_SIZE-pad;
            for (int i = 0; i < pad; ++i) {
                buffer[i] = 0;
            }
            out.write(buffer, 0, pad);
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        ++position;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        position += len;
    }

    @Override
    public void close() throws IOException {
        for (int i = 0; i < BLOCK_SIZE; ++i) {
            buffer[i] = 0;
        }
        out.write(buffer);
        out.write(buffer);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    private void setField(int offs, int len, String value)
            throws UnsupportedEncodingException {
        byte[] bytes = value.getBytes("ISO-8859-1");
        int n = Math.min(len-1, bytes.length);
        for (int i = 0; i < n; ++i) {
            buffer[offs+i] = bytes[i];
        }
        buffer[n] = 0;
    }
}
