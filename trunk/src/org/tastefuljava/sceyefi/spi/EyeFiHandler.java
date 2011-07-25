package org.tastefuljava.sceyefi.spi;

import org.tastefuljava.sceyefi.conf.EyeFiCard;

public interface EyeFiHandler {
    public UploadHandler startUpload(EyeFiCard card, String archiveName);
}
