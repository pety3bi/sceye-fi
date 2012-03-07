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
package org.tastefuljava.sceyefi.multipart;

import java.util.HashMap;
import java.util.Map;

public class ValueParser {
    private char chars[];
    private int pos;

    public static Map<String,String> parse(String s) {
        return new ValueParser(s).parse();
    }

    private ValueParser(char chars[]) {
        this.chars = chars;
    }

    private ValueParser(String s) {
        this(s.toCharArray());
    }

    private Map<String,String> parse() {
        Map<String,String> result = new HashMap<String,String>();
        while (pos < chars.length) {
            skipSpaces();
            if (pos >= chars.length) {
                break;
            }
            StringBuilder buf = new StringBuilder();
            int nameEnd = 0;
            char c = ' ';
            while (pos < chars.length) {
                c = chars[pos++];
                if (c == '=' || c == ';') {
                    break;
                }                
                if (c == '"') {
                    quotedString(buf);
                    nameEnd = buf.length();
                } else {
                    buf.append(c);
                    if (!Character.isWhitespace(c)) {
                        nameEnd = buf.length();
                    }
                }
            }
            
            String name = buf.substring(0, nameEnd).toLowerCase();
            skipSpaces();
            if (c != '=') {
                result.put("", name);
            } else {
                buf.setLength(0);
                int valEnd = 0;
                while (pos < chars.length) {
                    c = chars[pos++];
                    if (c == ';') {
                        break;
                    }
                    if (c == '"') {
                        quotedString(buf);
                        valEnd = buf.length();
                    } else {
                        buf.append(c);
                        if (!Character.isWhitespace(c)) {
                            valEnd = buf.length();
                        }
                    }
                }
                String value = buf.substring(0, valEnd);
                result.put(name.toLowerCase(), value);
            }
        }
        return result;
    }

    private void skipSpaces() {
        while (pos < chars.length && Character.isWhitespace(chars[pos])) {
            ++pos;
        }
    }

    private void quotedString(StringBuilder buf) {
        while (pos < chars.length) {
            char c = chars[pos++];
            if (c == '"') {
                break;
            } else if (c == '\\' && pos < chars.length) {
                c = chars[pos++];
            }
            buf.append(c);
        }
    }
}
