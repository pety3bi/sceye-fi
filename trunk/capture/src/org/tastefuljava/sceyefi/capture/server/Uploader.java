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
package org.tastefuljava.sceyefi.capture.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.sceyefi.capture.conf.EyeFiCard;
import org.tastefuljava.sceyefi.capture.conf.EyeFiConf;
import org.tastefuljava.sceyefi.capture.spi.EyeFiHandler;
import org.tastefuljava.sceyefi.capture.spi.UploadHandler;
import org.tastefuljava.sceyefi.capture.tar.TarEntry;
import org.tastefuljava.sceyefi.capture.tar.TarReader;
import org.tastefuljava.sceyefi.capture.util.Bytes;

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
                        upload.handleFile(te.getFileName(),
                                te.getLastModified(), in);
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

    void verifyDigest(byte[] digest) {
        if (calculatedDigest == null) {
            LOG.severe("No upload handler");
            failed = true;
        } else if (!Bytes.equals(digest, calculatedDigest)) {
            LOG.log(Level.SEVERE, 
                    "Integrity digest don''t match: "
                    + "actual={0}, expected={1}",
                    new Object[]{
                        Bytes.bin2hex(calculatedDigest),
                        Bytes.bin2hex(digest)
                    });
            failed = true;
        }
        success = !failed;
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
