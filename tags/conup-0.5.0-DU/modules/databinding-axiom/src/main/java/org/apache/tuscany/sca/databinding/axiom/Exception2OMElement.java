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
package org.apache.tuscany.sca.databinding.axiom;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.javabeans.Java2XMLMapperException;
import org.apache.tuscany.sca.databinding.javabeans.JavaBean2XMLTransformer;

/**
 *
 * @version $Rev: 796166 $ $Date: 2009-07-21 08:03:47 +0100 (Tue, 21 Jul 2009) $
 */
public class Exception2OMElement extends JavaBean2XMLTransformer<OMElement> {

    public static final String GETCAUSE = "getCause";
    public static final String GETLOCALIZEDMESSAGE = "getLocalizedMessage";
    public static final String GETSTACKTRACE = "getStackTrace";
    public static final String GETCLASS = "getClass";

    private OMFactory factory;

    public Exception2OMElement() {
        super();
        factory = OMAbstractFactory.getOMFactory();
    }

    @Override
    public OMElement transform(Object source, TransformationContext context) {
        OMElement element = super.transform(source, context);
        AxiomHelper.adjustElementName(context, element);
        return element;
    }

    @Override
    protected boolean isMappedGetter(String methodName) {
        if (GETCAUSE.equals(methodName)
            || GETLOCALIZEDMESSAGE.equals(methodName)
            || GETSTACKTRACE.equals(methodName)
            || GETCLASS.equals(methodName)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void appendChild(OMElement parentElement, OMElement childElement) throws Java2XMLMapperException {
        parentElement.addChild(childElement);
    }

    @Override
    public OMElement createElement(QName qName) throws Java2XMLMapperException {
        return factory.createOMElement(qName);
    }

    @Override
    public void appendText(OMElement parentElement, String textData) throws Java2XMLMapperException {
        if (textData == null) {
            OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            OMAttribute nil = factory.createOMAttribute("nil", xsi, "true");
            parentElement.addAttribute(nil);
        } else {
            factory.createOMText(parentElement, textData);
        }
    }

    @Override
    public Class getTargetType() {
        return OMElement.class;
    }

}
