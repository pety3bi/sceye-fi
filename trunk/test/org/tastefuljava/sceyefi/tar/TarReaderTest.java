package org.tastefuljava.sceyefi.tar;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TarReaderTest {
    private TarReader reader;

    @Before
    public void setUp() throws IOException {
        InputStream in = TarReaderTest.class.getResourceAsStream(
                "P1030001.JPG.tar");
        reader = new TarReader(in);
    }
    
    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void testNextEntry() throws Exception {
        System.out.println("nextEntry");
        TarEntry entry = reader.nextEntry();
        assertNotNull(entry);
        assertEquals("P1030001.JPG", entry.getFileName());
        InputStream in = entry.getInputStream();
        long readSize = 0;
        byte buf[] = new byte[1000];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            readSize += n;
        }
        in.close();
        assertEquals(entry.getLength(), readSize);
        entry = reader.nextEntry();
        assertNotNull(entry);
        assertEquals("P1030001.JPG.log", entry.getFileName());
        in = entry.getInputStream();
        readSize = 0;
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            readSize += n;
        }
        in.close();
        assertEquals(entry.getLength(), readSize);
        entry = reader.nextEntry();
        assertNull(entry);
    }
}
