package org.tastefuljava.sceyefi.spi;

import java.io.IOException;
import java.io.InputStream;

public interface UploadHandler {
    public void handleFile(String fileName, InputStream in)
            throws IOException;
    public void abort();
    public void commit();
}
