package org.tastefuljava.sceyefi.client;

import java.io.IOException;
import java.io.OutputStream;
import org.tastefuljava.sceyefi.server.ChecksumCalculator;

public class ChecksumOutputStream extends OutputStream {
    private OutputStream out;
    private ChecksumCalculator calc = new ChecksumCalculator();

    public ChecksumOutputStream(OutputStream out) {
        this.out = out;
    }

    public byte[] checksum(byte[] key) throws IOException {
        return calc.checksum(key);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        calc.processByte(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        calc.processBytes(b, off, len);
    }
}
