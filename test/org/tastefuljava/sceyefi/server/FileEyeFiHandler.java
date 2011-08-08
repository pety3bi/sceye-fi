package org.tastefuljava.sceyefi.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.tastefuljava.sceyefi.conf.EyeFiCard;
import org.tastefuljava.sceyefi.spi.EyeFiHandler;
import org.tastefuljava.sceyefi.spi.UploadHandler;

public class FileEyeFiHandler implements EyeFiHandler {
    private File folder;

    public FileEyeFiHandler(File folder) {
        this.folder = folder;
    }

    public UploadHandler startUpload(EyeFiCard card, String archiveName) {
        return new UploadHandler() {
            private List<File> files = new ArrayList<File>();

            public void handleFile(String fileName, Date timestamp,
                    InputStream in) throws IOException {
                File file = new File(folder, fileName);
                OutputStream out = new FileOutputStream(file);
                try {
                    byte buf[] = new byte[4096];
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
}
