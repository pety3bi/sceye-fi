package org.tastefuljava.sceyefi;

import java.io.File;
import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.server.EyeFiServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static File settings = null;

    public static void main(String[] args) {
        try {
            if (!parseArgs(args)) {
                usage();
                return;
            }
            EyeFiConf conf;
            if (settings == null) {
                conf = EyeFiConf.load();
            } else {
                conf = EyeFiConf.load(settings);
            }
            EyeFiServer.start(conf, new DiskFileHandler());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean parseArgs(String[] args) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.equals("-settings") && i < args.length) {
                settings = new File(args[i++]);
            } else {
                return false;
            }
        }
        return true;
    }

    private static void usage() {
        System.out.println(
                "Usage: java -jar sceye-fi.jar [-settings <settings-file>]");
    }
}
