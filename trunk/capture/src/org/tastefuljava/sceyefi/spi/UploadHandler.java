package org.tastefuljava.sceyefi.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface UploadHandler {
    public void handleFile(String fileName, Date timestamp, InputStream in)
            throws IOException;
    public void abort();
    public void commit();
}
