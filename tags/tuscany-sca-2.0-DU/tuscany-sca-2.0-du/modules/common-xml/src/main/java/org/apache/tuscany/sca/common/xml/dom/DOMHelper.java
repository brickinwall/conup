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
package org.apache.tuscany.sca.common.xml.dom;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.apache.tuscany.sca.common.xml.dom.impl.SAX2DOMAdapter;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.FactoryExtensionPoint;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Helper for DOM
 *
 * @version $Rev: 1151663 $ $Date: 2011-07-28 00:20:34 +0100 (Thu, 28 Jul 2011) $
 * @tuscany.spi.extension.asclient
 */
public class DOMHelper implements LifeCycleListener {
    protected static final int INITIAL_POOL_SIZE = 8;
    protected static final int MAX_POOL_SIZE = 64;
    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;
    protected ParserPool<DocumentBuilder> builderPool;
    protected ParserPool<Transformer> transformerPool;

    public static DOMHelper getInstance(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        return utilities.getUtility(DOMHelper.class);
    }

    public DOMHelper(ExtensionPointRegistry registry) {
        FactoryExtensionPoint factories = registry.getExtensionPoint(FactoryExtensionPoint.class);
        documentBuilderFactory = factories.getFactory(DocumentBuilderFactory.class);
        documentBuilderFactory.setNamespaceAware(true);
        transformerFactory = factories.getFactory(TransformerFactory.class);
    }

    /**
     * @param documentBuilderFactory
     * @param transformerFactory
     */
    public DOMHelper(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
        super();
        this.documentBuilderFactory = documentBuilderFactory;
        this.transformerFactory = transformerFactory;
    }

    public Document newDocument() {
        DocumentBuilder builder = newDocumentBuilder();
        try {
            return builder.newDocument();
        } finally {
            returnDocumentBuilder(builder);
        }

    }

    public DocumentBuilder newDocumentBuilder() {
        return builderPool.borrowFromPool();
    }

    public void returnDocumentBuilder(DocumentBuilder builder) {
        builderPool.returnToPool(builder);
    }

    private DocumentBuilder createDocumentBuilder() {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Document load(String xmlString) throws IOException, SAXException {
        DocumentBuilder builder = newDocumentBuilder();
        try {
            InputSource is = new InputSource(new StringReader(xmlString));
            return builder.parse(is);
        } finally {
            returnDocumentBuilder(builder);
        }
    }

    public Document load(Source source) {
        Transformer transformer = newTransformer();
        DOMResult result = new DOMResult(newDocument());
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        } finally {
            transformerPool.returnToPool(transformer);
        }
        return (Document)result.getNode();
    }

    public NodeContentHandler createContentHandler(Node root) {
        if (root == null) {
            root = newDocument();
        }
        return new SAX2DOMAdapter(root);
    }

    public String saveAsString(Node node) {
        Transformer transformer = newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        try {
            transformer.transform(new DOMSource(node), result);
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        } finally {
            returnTransformer(transformer);
        }
        return result.getWriter().toString();
    }

    public Transformer newTransformer() {
        return transformerPool.borrowFromPool();
    }

    public void returnTransformer(Transformer transformer) {
        transformerPool.returnToPool(transformer);
    }

    private Transformer createTransformer() {
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        return transformer;
    }

    public void saveAsSAX(Node node, ContentHandler contentHandler) {
        Transformer transformer = transformerPool.borrowFromPool();
        SAXResult result = new SAXResult(contentHandler);
        try {
            transformer.transform(new DOMSource(node), result);
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        } finally {
            returnTransformer(transformer);
        }
    }

    public static QName getQName(Node node) {
        String ns = node.getNamespaceURI();
        String prefix = node.getPrefix();
        String localName = node.getLocalName();
        if (localName == null) {
            localName = node.getNodeName();
        }
        if (ns == null) {
            ns = "";
        }
        if (prefix == null) {
            prefix = "";
        }
        return new QName(ns, localName, prefix);
    }

    public static Element createElement(Document document, QName name) {
        String prefix = name.getPrefix();
        String qname =
            (prefix != null && prefix.length() > 0) ? prefix + ":" + name.getLocalPart() : name.getLocalPart();
        return document.createElementNS(name.getNamespaceURI(), qname);
    }

    /**
     * Wrap an element as a DOM document
     * @param node
     * @return
     */
    public static Document promote(Node node) {
        if (node instanceof Document) {
            return (Document)node;
        }
        Element element = (Element)node;
        Document doc = element.getOwnerDocument();
        if (doc.getDocumentElement() == element) {
            return doc;
        }
        doc = (Document)element.getOwnerDocument().cloneNode(false);
        Element schema = (Element)doc.importNode(element, true);
        doc.appendChild(schema);
        Node parent = element.getParentNode();
        while (parent instanceof Element) {
            Element root = (Element)parent;
            NamedNodeMap nodeMap = root.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Attr attr = (Attr)nodeMap.item(i);
                String name = attr.getName();
                if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                    if (schema.getAttributeNode(name) == null) {
                        schema.setAttributeNodeNS((Attr)doc.importNode(attr, true));
                    }
                }
            }
            parent = parent.getParentNode();
        }
        return doc;
    }

    public static String getPrefix(Element element, String namespace) {
        if (element.isDefaultNamespace(namespace)) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }
        return element.lookupPrefix(namespace);
    }

    public static String getNamespaceURI(Element element, String prefix) {
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            prefix = null;
        }
        return element.lookupNamespaceURI(prefix);
    }

    public static interface NodeContentHandler extends ContentHandler, LexicalHandler {
        Node getNode();
    }

    @Override
    public void start() {
        builderPool = new ParserPool<DocumentBuilder>(MAX_POOL_SIZE, INITIAL_POOL_SIZE) {

            @Override
            protected DocumentBuilder newInstance() {
                return createDocumentBuilder();
            }

            @Override
            protected void resetInstance(DocumentBuilder obj) {
                obj.reset();
            }
        };

        transformerPool = new ParserPool<Transformer>(64, 8) {

            @Override
            protected Transformer newInstance() {
                return createTransformer();
            }

            @Override
            protected void resetInstance(Transformer obj) {
                obj.reset();
            }
        };
    }

    @Override
    public void stop() {
        builderPool.clear();
        transformerPool.clear();
    }

}
