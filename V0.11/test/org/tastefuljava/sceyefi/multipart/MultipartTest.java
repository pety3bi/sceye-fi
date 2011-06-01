package org.tastefuljava.sceyefi.multipart;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultipartTest {
    private static String TEST =
            "Content-type: multipart/form-data, boundary=AaB03x\r\n"
            + "\r\n"
            + "--AaB03x\r\n"
            + "content-disposition: form-data; name=\"field1\"\r\n"
            + "\r\n"
            + "Joe Blow\r\n"
            + "--AaB03x\r\n"
            + "content-disposition: form-data; name=\"pics\"; filename=\"file1.txt\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + " ... contents of file1.txt ...\r\n"
            + "--AaB03x--";
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
    public void testIterator() throws IOException {
        System.out.println("iterator");
        Multipart mp = new Multipart(in, "UTF-8", "AaB03x".getBytes());
        int i = 0;
        Part part = mp.nextPart();
        while (part != null) {
            InputStream is = part.getBody();
            String body = readText(is, "UTF-8");
            is.close();
            switch (i) {
                case 0:
                    assertEquals(part.getHeaders().size(), 1);
                    assertEquals(part.getValues("content-disposition").size(), 1);
                    assertEquals(part.getFirstValue("content-disposition"),
                            "form-data; name=\"field1\"");
                    assertEquals(body, "Joe Blow");
                    break;
                case 1:
                    assertEquals(part.getHeaders().size(), 2);
                    assertEquals(part.getValues("content-disposition").size(), 1);
                    assertEquals(part.getFirstValue("content-disposition"),
                            "form-data; name=\"pics\"; filename=\"file1.txt\"");
                    assertEquals(part.getValues("content-type").size(), 1);
                    assertEquals(part.getFirstValue("content-type"),
                            "text/plain");
                    assertEquals(body, " ... contents of file1.txt ...");
                    break;
                default:
                    fail("Too many parts");
            }
            ++i;
            part = mp.nextPart();
        }
        assertEquals(i, 2);
    }

    private static String readText(InputStream in, String charset)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            baos.write(buf, 0, n);
        }
        return new String(baos.toByteArray(), charset);
    }
}
