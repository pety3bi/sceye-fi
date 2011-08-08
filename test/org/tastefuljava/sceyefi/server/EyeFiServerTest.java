package org.tastefuljava.sceyefi.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tastefuljava.sceyefi.client.EyeFiClient;
import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.conf.EyeFiConfTest;
import org.tastefuljava.sceyefi.tar.TarReaderTest;

public class EyeFiServerTest {
    private static final URL SETTINGS_URL
            = EyeFiConfTest.class.getResource("Settings.xml");
    private static File tempDir;
    private static EyeFiServer server;
    private EyeFiClient client;
    
    public EyeFiServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        File tempFile = File.createTempFile("aaa", ".bbb");
        tempDir = tempFile.getParentFile();
        tempFile.delete();
        EyeFiConf conf = EyeFiConf.load(SETTINGS_URL);
        server = EyeFiServer.start(conf, new FileEyeFiHandler(tempDir));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.close();
    }

    @Before
    public void setUp() throws IOException {
        EyeFiConf conf = EyeFiConf.load(SETTINGS_URL);
        client = new EyeFiClient("localhost", conf.getCards()[0]);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIt() throws Exception {
        URL url = TarReaderTest.class.getResource("P1030001.JPG.tar");
        client.uploadArchive(url);
    }
}
