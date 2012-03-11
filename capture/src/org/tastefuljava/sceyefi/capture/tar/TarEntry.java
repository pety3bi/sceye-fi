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
package org.tastefuljava.sceyefi.capture.tar;

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
