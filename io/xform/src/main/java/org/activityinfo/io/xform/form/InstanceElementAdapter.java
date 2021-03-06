/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.io.xform.form;

import com.google.common.base.Strings;
import org.activityinfo.io.xform.Namespaces;
import org.w3c.dom.*;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class InstanceElementAdapter extends XmlAdapter<Object, InstanceElement> {

    private DocumentBuilder documentBuilder;

    public InstanceElementAdapter() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            documentBuilder = dbf.newDocumentBuilder();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InstanceElement unmarshal(Object v) throws Exception {
        Element element = (Element) v;
        return createInstanceElement(element);
    }

    private InstanceElement createInstanceElement(Element element) {
        InstanceElement instanceElement = new InstanceElement(element.getLocalName());
        instanceElement.setId(Strings.emptyToNull(element.getAttribute("id")));

        StringBuilder text = new StringBuilder();
        NodeList childNodes = element.getChildNodes();
        for(int i=0;i!=childNodes.getLength();++i) {
            Node child = childNodes.item(i);
            if(child instanceof Text) {
                text.append(((Text) child).getData());
            } else if(child instanceof Element) {
                instanceElement.getChildren().add(createInstanceElement((Element)child));
            }
        }

        if(text.length() > 0) {
            instanceElement.setValue(text.toString());
        }

        return instanceElement;
    }

    @Override
    public Object marshal(InstanceElement instanceEl) throws Exception {
        Document document = documentBuilder.newDocument();
        return createElement(document, instanceEl);
    }

    private Element createElement(Document document, InstanceElement instanceEl) {
        Element element = document.createElementNS(Namespaces.XFORM, instanceEl.getName());
        if(!Strings.isNullOrEmpty(instanceEl.getId())) {
            element.setAttribute("id", instanceEl.getId());
        }

        if(instanceEl.hasChildren()) {
            for(InstanceElement child : instanceEl.getChildren()) {
                element.appendChild(createElement(document, child));
            }
        } else {
            element.setTextContent(instanceEl.getValue());
        }
        return element;
    }
}
