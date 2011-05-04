package org.tastefuljava.sceyefi.server;

import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class SoapEnvelope {
    private static final Namespace SOAP_NS = Namespace.getNamespace("SOAP-ENV",
                    "http://schemas.xmlsoap.org/soap/envelope/");

    private SoapEnvelope() {
        // nothing
    }

    public static Element strip(Document doc) {
        Element env = doc.getRootElement();
        Element body = env.getChild("Body", SOAP_NS);
        @SuppressWarnings("unchecked")
        List<Element> children = body.getChildren();
        Element child = children.get(0);
        body.removeContent(child);
        return child;
    }

    public static Document wrap(Element elm) {
        Element body = new Element("Body", SOAP_NS);
        body.addContent(elm);
        Element env = new Element("Envelope", SOAP_NS);
        env.addContent(body);
        return new Document(env);
    }
}
