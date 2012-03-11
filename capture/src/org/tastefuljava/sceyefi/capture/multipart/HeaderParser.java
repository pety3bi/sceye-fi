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
package org.tastefuljava.sceyefi.capture.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeaderParser {
    private InputStream in;
    private String encoding;
    private Map<String,List<String>> headers
            = new LinkedHashMap<String,List<String>>();
    private int c;

    public static Map<String,List<String>> parse(
            InputStream in, String encoding) throws IOException {
        return new HeaderParser(in, encoding).parse();
    }

    private HeaderParser(InputStream in, String encoding)
            throws IOException {
        this.in = in;
        this.encoding = encoding;
        readc();
    }

    private Map<String,List<String>> parse() throws IOException {
        while (readHeader()) {
            // nothing
        }
        return headers;
    }

    private int readc() throws IOException {
        c = in.read();
        return c;
    }

    private boolean readHeader() throws IOException {
        if (c < 0) {
            throw new IOException("Malformed headers");
        }
        if (c == '\r') {
            if (readc() == '\n') {
                return false;
            } else {
                throw new IOException("Malformed headers");
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (c >= 0 && c != ':') {
            baos.write(c);
            readc();
        }
        baos.close();
        if (c < 0) {
            throw new IOException("Malformed headers");
        }
        String name = new String(baos.toByteArray(), encoding).toLowerCase();
        do {
            readc();
        } while (c == ' ' || c == '\t');
        baos.reset();
        while (true) {
            if (c < 0) {
                throw new IOException("Malformed headers");
            }
            if (c == '\r') {
                readc();
                if (c == '\n') {
                    readc();
                    if (c == ' ' || c == '\t') {
                        do {
                            readc();
                        } while (c == ' ' && c == '\t');
                    } else {
                        break;
                    }
                }
            } else {
                baos.write(c);
                readc();
            }
        }
        String value = new String(baos.toByteArray(), encoding);
        List<String> list = headers.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            headers.put(name, list);
        }
        list.add(value);
        return true;
    }
}
