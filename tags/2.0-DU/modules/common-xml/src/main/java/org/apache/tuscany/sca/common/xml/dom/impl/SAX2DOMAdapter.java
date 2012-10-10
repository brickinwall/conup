/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tuscany.sca.common.xml.dom.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.tuscany.sca.common.xml.dom.DOMHelper;
import org.apache.tuscany.sca.common.xml.dom.DOMHelper.NodeContentHandler;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX2DOM adapter
 *
 * @version $Rev: 801844 $ $Date: 2009-08-07 00:50:19 +0100 (Fri, 07 Aug 2009) $
 */
public class SAX2DOMAdapter implements DOMHelper.NodeContentHandler {
    public static final String EMPTYSTRING = "";
    public static final String XML_PREFIX = "xml";
    public static final String XMLNS_PREFIX = "xmlns";
    public static final String XMLNS_STRING = "xmlns:";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private Node root;

    private Document document;

    private Node nextSibling;

    private Stack<Node> nodeStk = new Stack<Node>();

    private List<String> namespaceDecls;

    private Node lastSibling;

    public SAX2DOMAdapter(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilities = registry.getExtensionPoint(UtilityExtensionPoint.class);
        DOMHelper domHelper = utilities.getUtility(DOMHelper.class);
        this.document = domHelper.newDocument();
        this.root = document;
    }

    public SAX2DOMAdapter(Node root, Node nextSibling) {
        this.root = root;
        if (root instanceof Document) {
            this.document = (Document)root;
        } else if (root != null) {
            this.document = root.getOwnerDocument();
        }

        this.nextSibling = nextSibling;
    }

    public SAX2DOMAdapter(Node root) {
        this(root, null);
    }

    public Node getNode() {
        return root;
    }

    public void characters(char[] ch, int start, int length) {
        final Node last = nodeStk.peek();

        // No text nodes can be children of root (DOM006 exception)
        if (last != document) {
            final String text = new String(ch, start, length);
            if (lastSibling != null && lastSibling.getNodeType() == Node.TEXT_NODE) {
                ((Text)lastSibling).appendData(text);
            } else if (last == root && nextSibling != null) {
                lastSibling = last.insertBefore(document.createTextNode(text), nextSibling);
            } else {
                lastSibling = last.appendChild(document.createTextNode(text));
            }

        }
    }

    public void startDocument() {
        nodeStk.push(root);
    }

    public void endDocument() {
        nodeStk.pop();
    }

    public void startElement(String namespace, String localName, String qName, Attributes attrs) {
        final Element tmp = document.createElementNS(namespace, qName);

        // Add namespace declarations first
        if (namespaceDecls != null) {
            final int nDecls = namespaceDecls.size();
            for (int i = 0; i < nDecls; i++) {
                final String prefix = namespaceDecls.get(i++);

                if (prefix == null || prefix.equals(EMPTYSTRING)) {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_PREFIX, namespaceDecls.get(i));
                } else {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_STRING + prefix, namespaceDecls.get(i));
                }
            }
            namespaceDecls.clear();
        }

        // Add attributes to element
        final int nattrs = attrs.getLength();
        for (int i = 0; i < nattrs; i++) {
            if (attrs.getLocalName(i) == null) {
                tmp.setAttribute(attrs.getQName(i), attrs.getValue(i));
            } else {
                tmp.setAttributeNS(attrs.getURI(i), attrs.getQName(i), attrs.getValue(i));
            }
        }

        // Append this new node onto current stack node
        Node last = nodeStk.peek();

        // If the SAX2DOM is created with a non-null next sibling node,
        // insert the result nodes before the next sibling under the root.
        if (last == root && nextSibling != null) {
            last.insertBefore(tmp, nextSibling);
        } else {
            last.appendChild(tmp);
        }

        // Push this node onto stack
        nodeStk.push(tmp);
        lastSibling = null;
    }

    public void endElement(String namespace, String localName, String qName) {
        nodeStk.pop();
        lastSibling = null;
    }

    public void startPrefixMapping(String prefix, String uri) {
        if (namespaceDecls == null) {
            namespaceDecls = new ArrayList<String>(2);
        }
        namespaceDecls.add(prefix);
        namespaceDecls.add(uri);
    }

    public void endPrefixMapping(String prefix) {
        // do nothing
    }

    /**
     * This class is only used internally so this method should never be called.
     */
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    /**
     * adds processing instruction node to DOM.
     */
    public void processingInstruction(String target, String data) {
        final Node last = nodeStk.peek();
        ProcessingInstruction pi = document.createProcessingInstruction(target, data);
        if (pi != null) {
            if (last == root && nextSibling != null) {
                last.insertBefore(pi, nextSibling);
            } else {
                last.appendChild(pi);
            }

            lastSibling = pi;
        }
    }

    /**
     * This class is only used internally so this method should never be called.
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * This class is only used internally so this method should never be called.
     */
    public void skippedEntity(String name) {
    }

    /**
     * Lexical Handler method to create comment node in DOM tree.
     */
    public void comment(char[] ch, int start, int length) {
        final Node last = nodeStk.peek();
        Comment comment = document.createComment(new String(ch, start, length));
        if (comment != null) {
            if (last == root && nextSibling != null) {
                last.insertBefore(comment, nextSibling);
            } else {
                last.appendChild(comment);
            }

            lastSibling = comment;
        }
    }

    // Lexical Handler methods- not implemented
    public void startCDATA() {
    }

    public void endCDATA() {
    }

    public void startEntity(java.lang.String name) {
    }

    public void endDTD() {
    }

    public void endEntity(String name) {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

}
