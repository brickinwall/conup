/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.sca.databinding.javabeans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Transformer to convert data from DOM Node to JavaBean
 *
 * @version $Rev: 796166 $ $Date: 2009-07-21 08:03:47 +0100 (Tue, 21 Jul 2009) $
 */
public class DOMNode2JavaBeanTransformer extends XML2JavaBeanTransformer<Node> {

    @Override
    public Node getRootElement(Node element) throws XML2JavaMapperException {
        if (element instanceof Document) {
            return ((Document)element).getDocumentElement();
        } else {
            return element;
        }

    }

    @Override
    public Iterator<Node> getChildElements(Node parent) throws XML2JavaMapperException {
        NodeList nodeList = parent.getChildNodes();
        List<Node> nodeArrayList = new ArrayList<Node>(nodeList.getLength());
        for (int count = 0; count < nodeList.getLength(); ++count) {
            nodeArrayList.add(nodeList.item(count));
        }

        return nodeArrayList.iterator();
    }

    @Override
    public String getElementName(Node element) throws XML2JavaMapperException {
        return element.getLocalName();
    }

    @Override
    public String getText(Node element) throws XML2JavaMapperException {
        if (element instanceof Document) {
            element = ((Document)element).getDocumentElement();
        }
        return element.getTextContent();
    }

    @Override
    public boolean isTextElement(Node element) throws XML2JavaMapperException {
        return element.getNodeType() == Node.TEXT_NODE;
    }

    @Override
    public boolean isTextOnly(Node element) throws XML2JavaMapperException {
        NodeList childNodes = element.getChildNodes();
        return childNodes.getLength() == 1 && isTextElement(childNodes.item(0));
    }

    @Override
    public Node getFirstChildWithName(Node element, QName name) throws XML2JavaMapperException {
        Node child;
        for (child = element.getFirstChild();
             child != null && !(child.getNodeName() == name.getLocalPart()
                                && child.getNamespaceURI() == name.getNamespaceURI()) ;
             child = child.getNextSibling()) {
        }
        return child;
    }

    @Override
    public Class getSourceType() {
        return Node.class;
    }
}
