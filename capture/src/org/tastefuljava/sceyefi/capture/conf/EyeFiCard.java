/*
    Sceye-Fi Photo capture
    Copyright (C) 2011-2012  Maurice Perry <maurice@perry.ch>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tastefuljava.sceyefi.capture.conf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.tastefuljava.sceyefi.capture.util.Bytes;

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
