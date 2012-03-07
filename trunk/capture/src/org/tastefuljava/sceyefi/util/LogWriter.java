package org.tastefuljava.sceyefi.util;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogWriter extends Writer {
    private Logger log;
    private Level level;
    private StringBuilder buf = new StringBuilder();

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
