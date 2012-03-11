package org.tastefuljava.sceyefi.util;

import org.tastefuljava.sceyefi.capture.util.Bytes;
import org.junit.Test;
import static org.junit.Assert.*;

public class BytesTest {
    private static final byte[] TEST_BYTES
            = {0x01, 0x23, 0x45, 0x67,
            (byte)0x89, (byte)0xAB, (byte)0xCD, (byte)0xEF};
    private static final String TEST_HEX = "0123456789abcdef";

    @Test
    public void testHexToBin() {
        System.out.println("hexToBin");
        assertArrayEquals(TEST_BYTES, Bytes.hex2bin(TEST_HEX));
    }

    @Test
    public void testBin2hex() {
        System.out.println("bin2hex");
        assertEquals(TEST_HEX, Bytes.bin2hex(TEST_BYTES));
    }

    @Test
    public void testBin2hex3() {
        System.out.println("bin2hex");
        assertEquals(TEST_HEX.substring(2, 2+12),
                Bytes.bin2hex(TEST_BYTES, 1, 6));
    }
}
