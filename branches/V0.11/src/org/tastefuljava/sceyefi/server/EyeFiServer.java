package org.tastefuljava.sceyefi.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import org.tastefuljava.sceyefi.conf.EyeFiCard;
import org.tastefuljava.sceyefi.conf.EyeFiConf;
import org.tastefuljava.sceyefi.conf.Media;
import org.tastefuljava.sceyefi.multipart.Multipart;
import org.tastefuljava.sceyefi.multipart.Part;
import org.tastefuljava.sceyefi.multipart.ValueParser;
import org.tastefuljava.sceyefi.tar.TarEntry;
import org.tastefuljava.sceyefi.tar.TarReader;
import org.tastefuljava.sceyefi.util.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.tastefuljava.sceyefi.util.LogWriter;

public class EyeFiServer {
    private static final Logger LOG
            = Logger.getLogger(EyeFiServer.class.getName());
    private static final int WORKERS = 2;
    private static final String MAIN_CONTEXT = "/api/soap/eyefilm/v1";
    public static final String UPLOAD_CONTEXT = "/api/soap/eyefilm/v1/upload";

    private byte[] snonce = Bytes.randomBytes(16);
    private String snonceStr = Bytes.bin2hex(snonce);
    private EyeFiConf conf;
    private ExecutorService executor;
    private HttpServer httpServer;
    private int lastFileId;

    public static EyeFiServer start(EyeFiConf conf) throws IOException {
        return new EyeFiServer(conf);
    }

    private EyeFiServer(EyeFiConf conf) throws IOException {
        this.conf = conf;
        InetSocketAddress addr = new InetSocketAddress(59278);
        httpServer = HttpServer.create(addr, 0);
        httpServer.createContext(MAIN_CONTEXT, new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleControl(exchange);
            }
        });
        httpServer.createContext(UPLOAD_CONTEXT, new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleUpload(exchange);
            }
        });
        boolean started = false;
        executor = Executors.newFixedThreadPool(WORKERS);
        try {
            httpServer.setExecutor(executor);
            httpServer.start();
            started = true;
            LOG.fine("Server started");
        } finally {
            if (!started) {
                executor.shutdownNow();
            }
        }
    }

    public void close() {
        httpServer.stop(30);
        executor.shutdownNow();
    }

    private void handleControl(HttpExchange exchange) throws IOException {
        try {
            Headers reqHeaders = exchange.getRequestHeaders();
            logHeaders(Level.FINE, reqHeaders);
            SAXBuilder builder = new SAXBuilder();
            Document request;
            InputStream in = exchange.getRequestBody();
            try {
                request = builder.build(in);
            } finally {
                in.close();
            }
            logXML(Level.FINE, request);
            Document response = handleRequest(request);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            logXML(Level.FINE, response);
            writeXML(response, bao, false);
            bao.close();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/xml; charset=\"utf-8\"");
            exchange.sendResponseHeaders(200, bao.size());
            OutputStream out = exchange.getResponseBody();
            try {
                bao.writeTo(out);
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (JDOMException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private void handleUpload(HttpExchange exchange) throws IOException {
        try {
            Headers reqHeaders = exchange.getRequestHeaders();
            logHeaders(Level.FINE, reqHeaders);
            String contentType = reqHeaders.getFirst("Content-Type");
            if (!contentType.startsWith("multipart/")) {
                throw new IOException("Multipart content required");
            }
            Map<String,String> parms = ValueParser.parse(contentType);
            String encoding = parms.get("charset");
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }
            byte boundary[] = parms.get("boundary").getBytes(encoding);
            InputStream in = exchange.getRequestBody();
            try {
                byte[] calculatedDigest = null;
                String success = "false";
                Document request = null;
                Element req = null;
                Multipart mp = new Multipart(in, encoding, boundary);
                Part part = mp.nextPart();
                while (part != null) {
                    InputStream is = part.getBody();
                    try {
                        String cd = part.getFirstValue("content-disposition");
                        Map<String,String> cdParms = ValueParser.parse(cd);
                        String fieldName = cdParms.get("name");
                        if (fieldName.equals("SOAPENVELOPE")) {
                            SAXBuilder builder = new SAXBuilder();
                            request = builder.build(is);
                            logXML(Level.FINE, request);
                            req = SoapEnvelope.strip(request);
                        } else if (fieldName.equals("FILENAME")) {
                            if (req == null) {
                                throw new IOException(
                                        "No SOAP envelope found for upload");
                            }
                            calculatedDigest = uploadFile(req, cdParms, is);
                        } else if (fieldName.equals("INTEGRITYDIGEST")) {
                            if (calculatedDigest == null) {
                                throw new IOException(
                                        "Cannot check integrity digest");
                            }
                            Reader reader = new InputStreamReader(is, encoding);
                            BufferedReader br = new BufferedReader(reader);
                            String s = br.readLine();
                            byte[] digest = Bytes.hex2bin(s);
                            if (Bytes.equals(digest, calculatedDigest)) {
                                success = "true";
                            } else {
                                LOG.log(Level.SEVERE, 
                                        "Integrity digest don''t match: "
                                        + "actual={0}, expected={1}",
                                        new Object[]{
                                            Bytes.bin2hex(calculatedDigest),
                                            s
                                        });
                            }
                        } else {
                            LOG.log(Level.WARNING, "Unknown field name: {0}",
                                    fieldName);
                            logHeaders(Level.FINE, part.getHeaders());
                            Reader reader = new InputStreamReader(is, encoding);
                            BufferedReader br = new BufferedReader(reader);
                            for (String s = br.readLine(); s != null;
                                    s = br.readLine()) {
                                LOG.fine(s);
                            }
                        }
                    } finally {
                        is.close();
                    }
                    part = mp.nextPart();
                }
            } finally {
                in.close();
            }
            Namespace eyefiNs = Namespace.getNamespace(
                    "ns1", "http://localhost/api/soap/eyefilm");
            Element resp = new Element("UploadPhotoResponse", eyefiNs);
            resp.addContent(new Element("success").setText("true"));
            Document response = SoapEnvelope.wrap(resp);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            logXML(Level.FINE, response);
            writeXML(response, bao, false);
            bao.close();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/xml; charset=\"utf-8\"");
            exchange.sendResponseHeaders(200, bao.size());
            OutputStream out = exchange.getResponseBody();
            try {
                bao.writeTo(out);
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (JDOMException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private Document handleRequest(Document request)
            throws IOException, JDOMException {
        Element req = SoapEnvelope.strip(request);
        String action = req.getName();
        Element resp;
        if ("StartSession".equals(action)) {
            resp = startSession(req);
        } else if ("GetPhotoStatus".equals(action)) {
            resp = getPhotoStatus(req);
        } else if ("MarkLastPhotoInRoll".equals(action)) {
            resp = markLastPhotoInRoll(req);
        } else {
            throw new IOException("Invalid action: " + action);
        }
        return SoapEnvelope.wrap(resp);
    }

    private Element startSession(Element req)
            throws JDOMException, IOException {
        String macAddress = req.getChildText("macaddress");
        EyeFiCard card = conf.getCard(macAddress);
        if (card == null) {
            throw new IOException("Card not found " + macAddress);
        }
        byte[] cnonce = Bytes.hex2bin(req.getChildText("cnonce"));
        String transferModeStr = req.getChildText("transfermode");
        String timestampStr = req.getChildText("transfermodetimestamp");

        byte[] credential = md5(
                Bytes.hex2bin(macAddress), cnonce, card.getUploadKey());
        String credentialStr = Bytes.bin2hex(credential);

        Namespace eyefiNs = Namespace.getNamespace(
                "ns1", "http://localhost/api/soap/eyefilm");
        Element resp = new Element("StartSessionResponse", eyefiNs);
        resp.addContent(new Element("credential").setText(credentialStr));
        resp.addContent(new Element("snonce").setText(snonceStr));
        resp.addContent(
                new Element("transfermode").setText(transferModeStr));
        resp.addContent(
                new Element("transfermodetimestamp").setText(timestampStr));
        resp.addContent(new Element("upsyncallowed").setText("false"));
        return resp;
    }

    private Element getPhotoStatus(Element req) throws IOException {
        String macAddress = req.getChildText("macaddress");
        EyeFiCard card = conf.getCard(macAddress);
        if (card == null) {
            throw new IOException("Card not found " + macAddress);
        }
        byte[] credential = md5(
                Bytes.hex2bin(macAddress), card.getUploadKey(), snonce);
        String expectedCred = Bytes.bin2hex(credential);
        String actualCred = req.getChildText("credential");
        if (!actualCred.equals(expectedCred)) {
            throw new IOException("Invalid credential send by the card");
        }
        Namespace eyefiNs = Namespace.getNamespace(
                "ns1", "http://localhost/api/soap/eyefilm");
        Element resp = new Element("GetPhotoStatusResponse", eyefiNs);
        int fileId = ++lastFileId;
        resp.addContent(new Element("fileid").setText("" + fileId));
        resp.addContent(new Element("offset").setText("0"));
        return resp;
    }

    private Element markLastPhotoInRoll(Element req) {
        Namespace eyefiNs = Namespace.getNamespace(
                "ns1", "http://localhost/api/soap/eyefilm");
        Element resp = new Element("MarkLastPhotoInRollResponse", eyefiNs);
        return resp;
    }

    private static byte[] md5(byte[]... args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (byte[] arg : args) {
                digest.update(arg);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EyeFiServer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private byte[] uploadFile(Element req, Map<String,String> cdParms,
            InputStream input) throws IOException {
        ChecksumInputStream stream = new ChecksumInputStream(input);
        String macAddress = req.getChildText("macaddress");
        EyeFiCard card = conf.getCard(macAddress);
        if (card == null) {
            throw new IOException("Card not found " + macAddress);
        }
        Media media = card.getMedia(Media.TYPE_PHOTO);
        if (media == null) {
            throw new IOException("No photo media in Eye-Fi settings");
        }
        File folder = media.getFolder();
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Could not create folder " + folder);
        }
        byte[] buf = new byte[4096];
        TarReader tr = new TarReader(stream);
        for (TarEntry te = tr.nextEntry(); te != null; te = tr.nextEntry()) {
            File file = new File(folder, te.getFileName());
            OutputStream out = new FileOutputStream(file);
            try {
                InputStream in = te.getInputStream();
                try {
                    for (int n = in.read(buf); n >= 0; n = in.read(buf)) {
                        out.write(buf, 0, n);
                    }
                } finally {
                    in.close();
                }
            } finally {
                out.close();
            }
        }
        return stream.checksum(card.getUploadKey());
    }

    private static void writeXML(Document doc, OutputStream out,
            boolean pretty) throws IOException {
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(pretty
                ? Format.getPrettyFormat()
                : Format.getCompactFormat());
        outp.output(doc, out);
    }

    private static void logXML(Level level, Document doc) throws IOException {
        if (LOG.isLoggable(level)) {
            Writer out = new LogWriter(LOG, level);
            try {
                XMLOutputter outp = new XMLOutputter();
                outp.setFormat(Format.getPrettyFormat());
                outp.output(doc, out);
            } finally {
                out.close();
            }
        }
    }

    private static void logHeaders(Level level,
            Map<String,List<String>> headers) {
        if (LOG.isLoggable(level)) {
            for (Map.Entry<String,List<String>> entry: headers.entrySet()) {
                String name = entry.getKey();
                for (String value: entry.getValue()) {
                    LOG.log(level, "{0}: {1}", new Object[]{name, value});
                }
            }
        }
    }
}