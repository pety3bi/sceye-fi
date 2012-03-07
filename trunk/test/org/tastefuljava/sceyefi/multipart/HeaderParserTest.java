package org.tastefuljava.sceyefi.multipart;

import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HeaderParserTest {
    private static String TEST =
            "content-disposition: form-data; name=\"pics\"; filename=\"file1.txt\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n";
    private InputStream in;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        in = new ByteArrayInputStream(TEST.getBytes("ASCII"));
    }
    
    @After
    public void tearDown() throws IOException {
        in.close();
    }

    @Test
    public void testReadHeaders() throws Exception {
        System.out.println("readHeaders");
        Map<String,List<String>> result = HeaderParser.parse(in, "ASCII");
        assertEquals(result.size(), 2);
        assertEquals(result.get("content-disposition").get(0),
                "form-data; name=\"pics\"; filename=\"file1.txt\"");
        assertEquals(result.get("content-type").get(0), "text/plain");
    }
}
