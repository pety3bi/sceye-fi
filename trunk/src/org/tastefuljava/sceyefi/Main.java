package org.tastefuljava.sceyefi;

import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.server.EyeFiServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            EyeFiConf conf = EyeFiConf.load();
            EyeFiServer.start(conf);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
