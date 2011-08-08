package org.tastefuljava.sceyefi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.tastefuljava.sceyefi.conf.EyeFiCard;
import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.server.SoapEnvelope;
import org.tastefuljava.sceyefi.util.Bytes;
import org.tastefuljava.sceyefi.util.LogWriter;

public class EyeFiClient {
    private static final Logger LOG
            = Logger.getLogger(EyeFiClient.class.getName());

    private static final int EYEFI_PORT = 59278;
    private static final Namespace REQUEST_NAMESPACE = Namespace.getNamespace(
            "EyeFi/SOAP/EyeFilm");

    private String hostName;
    private EyeFiCard card;
    private byte[] cnonce = Bytes.randomBytes(16);
    private byte[] snonce;

    public EyeFiClient(String hostName, EyeFiCard card) {
        this.hostName = hostName;
        this.card = card;
    }

    public void close() {
    }

    private HttpURLConnection createConnection(
            boolean multipart, String action) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append("http://");
        buf.append(hostName);
        buf.append(':');
        buf.append(EYEFI_PORT);
        buf.append("/api/soap/eyefilm/v1");
        if (multipart) {
            buf.append("/upload");
        }
        URL url = new URL(buf.toString());
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestProperty("SOAPAction", action);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        return con;
    }

    private Element simpleAction(Element req)
            throws IOException, JDOMException {
        HttpURLConnection con = createConnection(false, "urn:" + req.getName());
        try {
            OutputStream out = con.getOutputStream();
            try {
                XMLOutputter outp = new XMLOutputter();
                outp.output(SoapEnvelope.wrap(req), out);
            } finally {
                out.close();
            }
            InputStream in = con.getInputStream();
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(in);
                return SoapEnvelope.strip(doc);
            } finally {
                in.close();
            }
        } finally {
            con.disconnect();
        }
    }

    public void startSession() throws IOException, JDOMException {
        Element req = new Element("StartSession");
        req.addContent(new Element("macaddress").setText(card.getMacAddress()));
        req.addContent(new Element("cnonce").setText(Bytes.bin2hex(cnonce)));
        req.addContent(new Element("transfermode").setText(
                Integer.toString(card.getTransferMode())));
        req.addContent(new Element("transfermodetimestamp").setText(
                Long.toString(card.getTimestamp())));
        Element resp = simpleAction(req);
        logXML(Level.FINE, resp);
        byte[] credential = Bytes.md5(
                Bytes.hex2bin(card.getMacAddress()),
                cnonce,
                card.getUploadKey());
        byte[] actualCred = Bytes.hex2bin(resp.getChildText("credential"));
        if (!Bytes.equals(credential, actualCred)) {
            throw new IOException("Invalid credential");
        }
        snonce = Bytes.hex2bin(resp.getChildText("snonce"));
    }

    public void getPhotoStatus(String archiveName, long size)
            throws IOException, JDOMException {
        byte[] credential = Bytes.md5(
                Bytes.hex2bin(card.getMacAddress()),
                card.getUploadKey(),
                snonce);
        Element req = new Element("GetPhotoStatus");
        req.addContent(new Element("credential").setText(
                Bytes.bin2hex(credential)));
        req.addContent(new Element("macaddress").setText(card.getMacAddress()));
        req.addContent(new Element("filename").setText(archiveName));
        req.addContent(new Element("filesize").setText(Long.toString(size)));
        req.addContent(new Element("filesignature").setText(
                "343afd9e4e84d3d4f5969cd97214f7f2"));
        req.addContent(new Element("flags").setText("4"));
        Element resp = simpleAction(req);
        logXML(Level.FINE, resp);
    }

    public static void main(String args[]) {
        try {
            EyeFiConf conf = EyeFiConf.load();
            EyeFiCard card = conf.getCards()[0];
            EyeFiClient client = new EyeFiClient("localhost", card);
            client.startSession();
            client.getPhotoStatus("P1030007.JPG.tar", 1269760);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error", ex);
        }
    }

    private static void logXML(Level level, Element elm) throws IOException {
        if (LOG.isLoggable(level)) {
            Writer out = new LogWriter(LOG, level);
            try {
                XMLOutputter outp = new XMLOutputter();
                outp.setFormat(Format.getPrettyFormat());
                outp.output(elm, out);
            } finally {
                out.close();
            }
        }
    }
}
