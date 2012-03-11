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
package org.tastefuljava.sceyefi.capture;

import org.tastefuljava.sceyefi.capture.spi.UploadHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tastefuljava.sceyefi.capture.conf.EyeFiCard;
import org.tastefuljava.sceyefi.capture.conf.Media;
import org.tastefuljava.sceyefi.capture.spi.EyeFiHandler;

public class DiskFileHandler implements EyeFiHandler {
    private static final int BUFFER_SIZE = 4096;
    private static final Pattern NUMBERED_PATTERN
            = Pattern.compile("(^.*)\\(([0-9]+)\\)$");

    public UploadHandler startUpload(final EyeFiCard card, String archiveName) {
        return new UploadHandler() {
            private List<File> files = new ArrayList<File>();

            public void handleFile(String fileName, Date timestamp,
                    InputStream in) throws IOException {
                Media media = card.getMedia(Media.TYPE_PHOTO);
                if (media == null) {
                    throw new IOException("No photo media in Eye-Fi settings");
                }
                File folder = media.folderForDate(timestamp);
                if (!folder.isDirectory() && !folder.mkdirs()) {
                    throw new IOException("Could not create folder " + folder);
                }
                File file = uniqueFile(folder, fileName);
                OutputStream out = new FileOutputStream(file);
                try {
                    byte buf[] = new byte[BUFFER_SIZE];
                    for (int n = in.read(buf); n >= 0; n = in.read(buf)) {
                        out.write(buf, 0, n);
                    }
                } finally {
                    out.close();
                }
                files.add(file);
            }

            public void abort() {
                while (!files.isEmpty()) {
                    File file = files.remove(0);
                    file.delete();
                }
            }

            public void commit() {
                files.clear();
            }

        };
    }

    private static File uniqueFile(File folder, String fileName) {
        File file = new File(folder, fileName);
        if (file.exists()) {
            int extPos = fileName.lastIndexOf('.');
            String name = extPos < 0 ? fileName : fileName.substring(0, extPos);
            String ext = extPos < 0 ? "" : fileName.substring(extPos);
            int number = 0;
            Matcher matcher = NUMBERED_PATTERN.matcher(name);
            if (matcher.matches()) {
                name = matcher.group(1);
                number = Integer.parseInt(matcher.group(2));
            }
            do {
                ++number;
                file = new File(folder, name + "(" + number + ")" + ext);
            } while (file.exists());
        }
        return file;
    }
}
