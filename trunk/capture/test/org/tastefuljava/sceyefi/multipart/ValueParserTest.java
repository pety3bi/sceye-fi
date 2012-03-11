package org.tastefuljava.sceyefi.multipart;

import org.tastefuljava.sceyefi.capture.multipart.ValueParser;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValueParserTest {
    @Test
    public void testParse() {
        System.out.println("parse");
        Map<String,String> result = ValueParser.parse(
                "multipart/form; boundary=\"abcxyz\"");
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("boundary"));
        assertEquals(result.get("boundary"), "abcxyz");
        assertEquals(result.get(""), "multipart/form");
    }
}
