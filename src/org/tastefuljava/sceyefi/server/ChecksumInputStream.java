package org.tastefuljava.sceyefi.server;

import java.io.IOException;
import java.io.InputStream;

public class ChecksumInputStream extends InputStream {
    private InputStream in;
    private boolean eof;
    private ChecksumCalculator calc = new ChecksumCalculator();

    public ChecksumInputStream(InputStream in) {
        this.in = in;
    }

    public byte[] checksum(byte[] key) throws IOException {
        while (!eof) {
            read();
        }
        return calc.checksum(key);
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b < 0) {
            eof = true;
            return b;
        }
        calc.processByte(b);
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        calc.processBytes(b, off, n);
        return n;
    }
}
