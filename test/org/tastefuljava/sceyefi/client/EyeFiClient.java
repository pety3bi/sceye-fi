package org.tastefuljava.sceyefi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
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
import org.tastefuljava.sceyefi.conf.EyeFiConfTest;
import org.tastefuljava.sceyefi.server.ChecksumInputStream;
import org.tastefuljava.sceyefi.server.SoapEnvelope;
import org.tastefuljava.sceyefi.tar.TarReaderTest;
import org.tastefuljava.sceyefi.util.Bytes;
import org.tastefuljava.sceyefi.util.LogWriter;

public class EyeFiClient {
    private static final Logger LOG
            = Logger.getLogger(EyeFiClient.class.getName());

    private static final int EYEFI_PORT = 59278;
    private static final Namespace REQUEST_NAMESPACE = Namespace.getNamespace(
            "ns1", "EyeFi/SOAP/EyeFilm");

    private String hostName;
    private EyeFiCard card;
    private byte[] cnonce = Bytes.randomBytes(16);
    private byte[] snonce;
    private long fileId;

    public EyeFiClient(String hostName, EyeFiCard card) {
        this.hostName = hostName;
        this.card = card;
    }

    public void close() {
    }

    private HttpURLConnection createConnection(boolean multipart)
            throws IOException {
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
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-agent", "Eye-Fi Card/4.5022");
        con.setRequestProperty("Host", "api.eye.fi");
        return con;
    }

    private Element simpleAction(Element req)
            throws IOException, JDOMException {
        HttpURLConnection con = createConnection(false);
        try {
            con.setRequestProperty("SOAPAction", "urn:" + req.getName());
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
        Element req = new Element("StartSession", REQUEST_NAMESPACE);
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
        Element req = new Element("GetPhotoStatus", REQUEST_NAMESPACE);
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
        fileId = Long.parseLong(resp.getChildText("fileid"));
    }

    public void uploadArchive(InputStream input, String fileName, long size,
            Date timestamp) throws IOException, JDOMException {
        HttpURLConnection con = createConnection(true);
        try {
            String boundary = "aaaaaaaaaaaaazzzzzzzzzz";
            con.setRequestProperty("Content-type",
                    "multipart/form-data; boundary=" + boundary);
            OutputStream out = con.getOutputStream();
            try {
                // Envelope
                Element req = new Element("UploadPhoto", REQUEST_NAMESPACE);
                req.addContent(new Element("fileid").setText(
                        Long.toString(fileId)));
                req.addContent(new Element("macaddress").setText(
                        card.getMacAddress()));
                req.addContent(new Element("filename").setText(fileName));
                req.addContent(new Element("filesize").setText(
                        Long.toString(size)));
                req.addContent(new Element("filesignature").setText(
                        "c8340300c434030000000000dced0300"));
                req.addContent(new Element("encryption").setText("none"));
                req.addContent(new Element("flags").setText("4"));
                out.write(("\r\n--" + boundary + "\r\n").getBytes("ASCII"));
                out.write("Content-Disposition: form-data; name=\"SOAPENVELOPE\"\r\n\r\n".getBytes("ASCII"));
                XMLOutputter outp = new XMLOutputter();
                outp.output(SoapEnvelope.wrap(req), out);
                out.write(("\r\n--" + boundary + "\r\n").getBytes("ASCII"));
                out.write(("Content-Disposition: form-data; name=\"FILENAME\"\r\n"
                        + "Content-Type: application/x-tar\r\n\r\n").getBytes("ASCII"));
                ChecksumInputStream in = new ChecksumInputStream(input);
                try {
                    byte buf[] = new byte[4096];
                    for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                        out.write(buf, 0, n);
                    }
                } finally {
                    in.close();
                }
                byte[] digest = in.checksum(card.getUploadKey());
                out.write(("\r\n--" + boundary + "\r\n").getBytes("ASCII"));
                out.write("Content-Disposition: form-data; name=\"INTEGRITYDIGEST\"\r\n\r\n".getBytes("ASCII"));
                out.write(Bytes.bin2hex(digest).getBytes("ASCII"));
                out.write(("\r\n--" + boundary + "--").getBytes("ASCII"));
            } finally {
                out.close();
            }
            InputStream in = con.getInputStream();
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(in);
                Element resp = SoapEnvelope.strip(doc);
                logXML(Level.FINE, resp);
                boolean success = "true".equalsIgnoreCase(
                        resp.getChildText("success"));
                if (!success) {
                    throw new IOException("Upload failed");
                }
            } finally {
                in.close();
            }            
        } finally {
            con.disconnect();
        }
    }

    public void markLastPhotoInRoll() throws JDOMException, IOException {
        Element req = new Element("MarkLastPhotoInRoll", REQUEST_NAMESPACE);
        req.addContent(new Element("macaddress").setText(card.getMacAddress()));
        req.addContent(new Element("mergedelta").setText("0"));
        Element resp = simpleAction(req);
        logXML(Level.FINE, resp);
    }

    public void uploadArchive(URL url) throws IOException, JDOMException {
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        try {
            startSession();
            getPhotoStatus(url.getFile(), con.getContentLength());
            uploadArchive(in, url.getFile(), con.getContentLength(),
                    new Date(con.getLastModified()));
            markLastPhotoInRoll();
        } finally {
            in.close();
        }
    }

    public static void main(String args[]) {
        try {
            URL confURL = EyeFiConfTest.class.getResource("Settings.xml");
            EyeFiConf conf = EyeFiConf.load(confURL);
            EyeFiCard card = conf.getCards()[0];
            EyeFiClient client = new EyeFiClient("localhost", card);
            URL url = TarReaderTest.class.getResource("P1030001.JPG.tar");
            client.uploadArchive(url);
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
