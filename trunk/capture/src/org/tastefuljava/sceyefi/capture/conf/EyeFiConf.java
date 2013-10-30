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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class EyeFiConf {
    private final Map<String,EyeFiCard> cards = new HashMap<String,EyeFiCard>();

    public static EyeFiConf load() throws IOException {
        File homeDir = new File(System.getProperty("user.home"));
        File file = new File(homeDir,
                "Application Data/Eye-Fi/Settings.xml"); // Windoze
        if (!file.isFile()) {
            file = new File(homeDir, "Library/Eye-Fi/Settings.xml"); // Mac
            if (!file.isFile()) {
                throw new IOException("Eye-Fi/Settings.xml not found");
            }
        }
        return load(file);
    }

    public static EyeFiConf load(File file) throws IOException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(file);
            return new EyeFiConf(doc);
        } catch (JDOMException ex) {
            Logger.getLogger(EyeFiConf.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    public static EyeFiConf load(URL url) throws IOException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(url);
            return new EyeFiConf(doc);
        } catch (JDOMException ex) {
            Logger.getLogger(EyeFiConf.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private EyeFiConf(Document doc) throws JDOMException {
        Element configElm = doc.getRootElement();
        Element cardsElm = configElm.getChild("Cards");
        @SuppressWarnings("unchecked")
        List<Element> cardElms = cardsElm.getChildren("Card");
        for (Element cardElm: cardElms) {
            EyeFiCard card = new EyeFiCard(cardElm);
            cards.put(card.getMacAddress(), card);
        }
    }

    public EyeFiCard[] getCards() {
        return cards.values().toArray(new EyeFiCard[cards.size()]);
    }

    public EyeFiCard getCard(String macAddress) {
        return cards.get(macAddress);
    }
}
