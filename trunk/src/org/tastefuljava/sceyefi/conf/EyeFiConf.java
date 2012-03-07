package org.tastefuljava.sceyefi.conf;

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
    private Map<String,EyeFiCard> cards = new HashMap<String,EyeFiCard>();

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

    public EyeFiCard getCard(String macAddress) {
        return cards.get(macAddress);
    }
}
