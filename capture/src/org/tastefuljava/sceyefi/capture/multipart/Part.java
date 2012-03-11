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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Part {
    private Map<String,List<String>> headers;
    private InputStream body;

    Part(Map<String,List<String>> headers, InputStream body) {
        this.headers = headers;
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getValues(String name) {
        return headers.get(name.toLowerCase());
    }

    public String getFirstValue(String name) {
        List<String> values = getValues(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public InputStream getBody() {
        return body;
    }

    public void close() throws IOException {
        body.close();
    }
}
