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

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.common.xml.dom.DOMHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Transformer to convert data from a JavaBean object to DOM Node
 *
 * @version $Rev: 801902 $ $Date: 2009-08-07 08:37:15 +0100 (Fri, 07 Aug 2009) $
 */
public class JavaBean2DOMNodeTransformer extends JavaBean2XMLTransformer<Node> {

    public static final String COLON = ":";
    private DOMHelper helper;
    
    public JavaBean2DOMNodeTransformer(ExtensionPointRegistry registry) {
        super();
        helper = DOMHelper.getInstance(registry);
    }
    
    @Override
    public void appendChild(Node parentElement, Node childElement) throws Java2XMLMapperException {
        parentElement.appendChild(childElement);
    }

    @Override
    public Node createElement(QName qName) throws Java2XMLMapperException {
        String qualifedName =
            (qName.getPrefix() == null || qName.getPrefix().length() <= 0) ? qName.getLocalPart()
                : qName.getPrefix() + COLON + qName.getLocalPart();
        return helper.newDocument().createElementNS(qName.getNamespaceURI(), qualifedName);
    }

    @Override
    public void appendText(Node parentElement, String textData) throws Java2XMLMapperException {
        Document document = helper.newDocument();
        Node textNode;
        if (textData != null) {
            textNode = document.createTextNode(textData);
        } else {
            Attr nil = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:nil");
            nil.setValue("true");
            textNode = nil;
        }
        appendChild(parentElement, textNode);
    }

    @Override
    public Class getTargetType() {
        return Node.class;
    }

}
