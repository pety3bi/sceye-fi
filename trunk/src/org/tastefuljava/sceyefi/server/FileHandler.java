package org.tastefuljava.sceyefi.server;

import java.io.IOException;
import java.io.InputStream;
import org.tastefuljava.sceyefi.conf.EyeFiCard;

public interface FileHandler {
    public void handleFile(EyeFiCard card, String fileName, InputStream in)
            throws IOException;
}
