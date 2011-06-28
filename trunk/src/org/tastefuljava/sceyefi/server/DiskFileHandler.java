package org.tastefuljava.sceyefi.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.tastefuljava.sceyefi.conf.EyeFiCard;
import org.tastefuljava.sceyefi.conf.Media;

public class DiskFileHandler implements FileHandler {
    private static final int BUFFER_SIZE = 4096;

    public void handleFile(EyeFiCard card, String fileName, InputStream in)
            throws IOException {
        Media media = card.getMedia(Media.TYPE_PHOTO);
        if (media == null) {
            throw new IOException("No photo media in Eye-Fi settings");
        }
        File folder = media.getFolder();
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Could not create folder " + folder);
        }
        File file = new File(folder, fileName);
        OutputStream out = new FileOutputStream(file);
        try {
            byte buf[] = new byte[BUFFER_SIZE];
            for (int n = in.read(buf); n >= 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }
        } finally {
            out.close();
        }
    }
}
