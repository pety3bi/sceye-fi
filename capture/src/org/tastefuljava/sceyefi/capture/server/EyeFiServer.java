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
package org.tastefuljava.sceyefi.capture.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import org.tastefuljava.sceyefi.capture.conf.EyeFiCard;
import org.tastefuljava.sceyefi.capture.conf.EyeFiConf;
import org.tastefuljava.sceyefi.capture.multipart.Multipart;
import org.tastefuljava.sceyefi.capture.multipart.Part;
import org.tastefuljava.sceyefi.capture.multipart.ValueParser;
import org.tastefuljava.sceyefi.capture.util.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
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
import org.tastefuljava.sceyefi.capture.spi.EyeFiHandler;
import org.tastefuljava.sceyefi.capture.util.LogWriter;

public class EyeFiServer {
    private static final Logger LOG
            = Logger.getLogger(EyeFiServer.class.getName());

    private static final int EYEFI_PORT = 59278;
    private static final int WORKERS = 2;
    private static final String MAIN_CONTEXT = "/api/soap/eyefilm/v1";
    private static final String UPLOAD_CONTEXT = "/api/soap/eyefilm/v1/upload";
    private static final Namespace EYEFI_NAMESPACE = Namespace.getNamespace(
                    "ns1", "http://localhost/api/soap/eyefilm");

    private byte[] snonce = Bytes.randomBytes(16);
    private String snonceStr = Bytes.bin2hex(snonce);
    private EyeFiConf conf;
    private ExecutorService executor;
    private HttpServer httpServer;
    private int lastFileId;
    private EyeFiHandler handler;

    public static EyeFiServer start(EyeFiConf conf, EyeFiHandler handler)
            throws IOException {
        return new EyeFiServer(conf, handler);
    }

    private EyeFiServer(EyeFiConf conf, EyeFiHandler handler)
            throws IOException {
        this.conf = conf;
        this.handler = handler;
        InetSocketAddress addr = new InetSocketAddress(EYEFI_PORT);
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
        httpServer.removeContext(UPLOAD_CONTEXT);
        httpServer.removeContext(MAIN_CONTEXT);
        httpServer.stop(10);
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
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        } catch (JDOMException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
            boolean success;
            InputStream in = exchange.getRequestBody();
            try {
                byte boundary[] = parms.get("boundary").getBytes(encoding);
                Multipart mp = new Multipart(in, encoding, boundary);
                success = processParts(mp, encoding);
            } finally {
                in.close();
            }
            Element resp = new Element("UploadPhotoResponse", EYEFI_NAMESPACE);
            resp.addContent(new Element("success").setText(
                    success ? "true" : "false"));
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
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw ex;
        } catch (JDOMException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private boolean processParts(Multipart mp, String encoding)
            throws IOException, JDOMException {
        boolean success;
        Uploader uploader = new Uploader(conf, handler);
        try {
            for (Part part = mp.nextPart(); part != null;
                    part = mp.nextPart()) {
                processPart(uploader, part, encoding);
            }
        } finally {
            success = uploader.close();
        }
        return success;
    }

    private void processPart(Uploader uploader, Part part, String encoding)
            throws JDOMException, IOException {
        logHeaders(Level.FINE, part.getHeaders());
        InputStream is = part.getBody();
        try {
            String cd = part.getFirstValue("content-disposition");
            Map<String,String> cdParms = ValueParser.parse(cd);
            String fieldName = cdParms.get("name");
            if (fieldName.equals("SOAPENVELOPE")) {
                SAXBuilder builder = new SAXBuilder();
                Document request = builder.build(is);
                logXML(Level.FINE, request);
                Element req = SoapEnvelope.strip(request);
                uploader.start(
                        childText(req, "macaddress"),
                        childText(req, "filename"));
            } else if (fieldName.equals("FILENAME")) {
                uploader.upload(is);
            } else if (fieldName.equals("INTEGRITYDIGEST")) {
                Reader reader = new InputStreamReader(is, encoding);
                BufferedReader br = new BufferedReader(reader);
                String s = br.readLine();
                byte[] digest = Bytes.hex2bin(s);
                uploader.verifyDigest(digest);
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
        String macAddress = childText(req, "macaddress");
        EyeFiCard card = conf.getCard(macAddress);
        if (card == null) {
            throw new IOException("Card not found " + macAddress);
        }
        byte[] cnonce = Bytes.hex2bin(childText(req, "cnonce"));
        String transferModeStr = childText(req, "transfermode");
        String timestampStr = childText(req, "transfermodetimestamp");

        byte[] credential = Bytes.md5(
                Bytes.hex2bin(macAddress), cnonce, card.getUploadKey());
        String credentialStr = Bytes.bin2hex(credential);

        Element resp = new Element("StartSessionResponse", EYEFI_NAMESPACE);
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
        String macAddress = childText(req, "macaddress");
        EyeFiCard card = conf.getCard(macAddress);
        if (card == null) {
            throw new IOException("Card not found " + macAddress);
        }
        byte[] credential = Bytes.md5(
                Bytes.hex2bin(macAddress), card.getUploadKey(), snonce);
        String expectedCred = Bytes.bin2hex(credential);
        String actualCred = childText(req, "credential");
        if (!actualCred.equals(expectedCred)) {
            throw new IOException("Invalid credential send by the card");
        }
        Element resp = new Element("GetPhotoStatusResponse", EYEFI_NAMESPACE);
        int fileId = ++lastFileId;
        resp.addContent(new Element("fileid").setText("" + fileId));
        resp.addContent(new Element("offset").setText("0"));
        return resp;
    }

    private Element markLastPhotoInRoll(Element req) {
        Element resp = new Element("MarkLastPhotoInRollResponse", EYEFI_NAMESPACE);
        return resp;
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

    private static String childText(Element elm, String name) {
        String s = elm.getChildText(name);
        if (s != null) {
            return s;
        }
        return elm.getChildText(name, elm.getNamespace());
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
