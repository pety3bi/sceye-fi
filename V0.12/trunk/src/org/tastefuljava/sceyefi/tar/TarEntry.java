package org.tastefuljava.sceyefi.tar;

import java.io.InputStream;
import java.util.Date;

public class TarEntry {
    private String fileName;
    private Date lastModified;
    private long length;
    private InputStream in;

    TarEntry(String fileName, Date lastModified, long length, InputStream in) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.length = length;
        this.in = in;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public long getLength() {
        return length;
    }

    public InputStream getInputStream() {
        return in;
    }
}
