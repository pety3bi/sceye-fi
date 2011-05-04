package org.tastefuljava.sceyefi.tar;

import java.io.InputStream;

public class TarEntry {
    private String fileName;
    private long lastModified;
    private long length;
    private InputStream in;

    TarEntry(String fileName, long lastModified, long length, InputStream in) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.length = length;
        this.in = in;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLength() {
        return length;
    }

    public InputStream getInputStream() {
        return in;
    }
}
