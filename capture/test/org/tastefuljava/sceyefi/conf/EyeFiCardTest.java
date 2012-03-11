package org.tastefuljava.sceyefi.conf;

import org.tastefuljava.sceyefi.capture.conf.EyeFiConf;
import org.tastefuljava.sceyefi.capture.conf.EyeFiCard;
import org.tastefuljava.sceyefi.capture.conf.Media;
import org.tastefuljava.sceyefi.capture.util.Bytes;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EyeFiCardTest {
    private EyeFiCard card;
    
    @Before
    public void setUp() throws IOException {
        EyeFiConf conf = EyeFiConf.load(getClass().getResource("Settings.xml"));
        card = conf.getCard("001856417729");
    }

    @Test
    public void testMacAddress() {
        System.out.println("macAddress");
        assertEquals("001856417729", card.getMacAddress());
    }

    @Test
    public void testUploadKey() {
        System.out.println("uploadKey");
        assertArrayEquals(Bytes.hex2bin("0d6ec517a597ec130b64e77c9b57f40e"),
                card.getUploadKey());
    }

    @Test
    public void testDownsyncKey() {
        System.out.println("downsyncKey");
        assertArrayEquals(Bytes.hex2bin("693db4cdaa431bb3b1132dfe89c60755"),
                card.getDownsyncKey());
    }

    @Test
    public void testTransferMode() {
        System.out.println("transferMode");
        assertEquals(2, card.getTransferMode());
    }

    @Test
    public void testTimestamp() {
        System.out.println("timestamp");
        assertEquals(1304088542L, card.getTimestamp());
    }

    @Test
    public void testMedias() {
        System.out.println("medias");
        assertEquals(1, card.getMedias().size());
        assertTrue(card.hasMedia(Media.TYPE_PHOTO));
        assertNotNull(card.getMedia(Media.TYPE_PHOTO));
    }
}
