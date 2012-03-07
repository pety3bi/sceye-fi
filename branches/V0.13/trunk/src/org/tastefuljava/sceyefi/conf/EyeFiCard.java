package org.tastefuljava.sceyefi.conf;

import org.tastefuljava.sceyefi.util.Bytes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

public class EyeFiCard {
    private String macAddress;
    private byte[] uploadKey;
    private byte[] downsyncKey;
    private int transferMode;
    private long timestamp;
    private Map<Integer,Media> mediaTypes = new LinkedHashMap<Integer,Media>();

    EyeFiCard(Element cardElm) {
        String mac = cardElm.getAttributeValue("MacAddress");
        macAddress = mac.replace("-","");
        uploadKey = Bytes.hex2bin(cardElm.getChildText("UploadKey"));
        downsyncKey = Bytes.hex2bin(cardElm.getChildText("DownsyncKey"));
        transferMode = Integer.parseInt(cardElm.getChildText("TransferMode"));
        timestamp = Long.parseLong(
                cardElm.getChildText("TransferModeTimestamp"));
        Element medias = cardElm.getChild("MediaTypes");
        if (medias != null) {
            @SuppressWarnings("unchecked")
            List<Element> list = medias.getChildren("Media");
            for (Element elm: list) {
                Media media = new Media(elm);
                mediaTypes.put(media.getType(), media);
            }
        }
    }

    public String getMacAddress() {
        return macAddress;
    }

    public byte[] getUploadKey() {
        return uploadKey;
    }

    public byte[] getDownsyncKey() {
        return downsyncKey;
    }

    public int getTransferMode() {
        return transferMode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Collection<Media> getMedias() {
        return new ArrayList<Media>(mediaTypes.values());
    }

    public boolean hasMedia(int type) {
        return mediaTypes.containsKey(type);
    }

    public Media getMedia(int type) {
        return mediaTypes.get(type);
    }
}
