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
package org.tastefuljava.sceyefi.capture.util;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogWriter extends Writer {
    @SuppressWarnings("NonConstantLogger")
    private final Logger log;
    private final Level level;
    private final StringBuilder buf = new StringBuilder();

    public LogWriter(Logger log, Level level) {
        this.log = log;
        this.level = level;
    }

    @Override
    public void write(char[] chars, int off, int len) throws IOException {
        int end = off + len;
        for (int i = off; i < end; ++i) {
            char c = chars[i];
            if (c == '\r') {
                // ignore
            } else if (c == '\n') {
                flush();
            } else {
                buf.append(c);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        log.log(level, buf.toString());
        buf.setLength(0);
    }

    @Override
    public void close() throws IOException {
        if (buf.length() > 0) {
            flush();
        }
    }
}
