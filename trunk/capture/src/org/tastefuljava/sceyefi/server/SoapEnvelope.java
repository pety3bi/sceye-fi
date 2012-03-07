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
