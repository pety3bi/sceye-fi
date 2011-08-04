package org.tastefuljava.sceyefi.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.sceyefi.conf.EyeFiCard;
import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.spi.EyeFiHandler;
import org.tastefuljava.sceyefi.spi.UploadHandler;
import org.tastefuljava.sceyefi.tar.TarEntry;
import org.tastefuljava.sceyefi.tar.TarReader;
import org.tastefuljava.sceyefi.util.Bytes;

/**
 * Handles the upload of a tar file
 */
class Uploader {
    private static final Logger LOG
            = Logger.getLogger(Uploader.class.getName());

    private EyeFiConf conf;
    private EyeFiHandler handler;
    private EyeFiCard card;
    private UploadHandler upload;
    private boolean failed;
    private boolean success;
    private byte[] calculatedDigest;

    Uploader(EyeFiConf conf, EyeFiHandler handler) {
        this.conf = conf;
        this.handler = handler;
    }

    void start(String macAddress, String arcName) {
        if (macAddress == null) {
            LOG.severe("No mac address in request");
            failed = true;
        } else {
            card = conf.getCard(macAddress);
            if (card == null) {
                LOG.log(Level.SEVERE, "Card not found {0}", macAddress);
                failed = true;
            }
            upload = handler.startUpload(card, arcName);
        }
    }

    void upload(InputStream tar) throws IOException {
        ChecksumInputStream stream = new ChecksumInputStream(tar);
        TarReader tr = new TarReader(stream);
        for (TarEntry te = tr.nextEntry(); te != null; te = tr.nextEntry()) {
            InputStream in = te.getInputStream();
            try {
                if (!failed) {
                    if (upload == null) {
                        failed = true;
                    } else {
                        upload.handleFile(te.getFileName(), in);
                    }
                }
            } finally {
                in.close();
            }
        }
        if (card != null) {
            calculatedDigest = stream.checksum(card.getUploadKey());
        }
    }

    void verifyChecksum(byte[] digest) {
        if (calculatedDigest == null) {
            LOG.severe("No upload handler");
            failed = true;
            success = false;
        } else if (Bytes.equals(digest, calculatedDigest)) {
            success = !failed;
        } else {
            LOG.log(Level.SEVERE, 
                    "Integrity digest don''t match: "
                    + "actual={0}, expected={1}",
                    new Object[]{
                        Bytes.bin2hex(calculatedDigest),
                        Bytes.bin2hex(digest)
                    });
            failed = true;
            success = false;
        }
    }

    boolean close() {
        if (upload != null) {
            if (success) {
                upload.commit();
                return true;
            } else {
                upload.abort();
            }
        }
        return false;
    }
}
