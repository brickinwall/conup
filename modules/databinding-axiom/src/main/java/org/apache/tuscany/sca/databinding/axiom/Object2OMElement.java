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
import org.apache.tuscany.sca.databinding.impl.Java2SimpleTypeTransformer;

/**
 * Transformer to convert data from an simple OMElement to Java Object
 *
 * @version $Rev: 656164 $ $Date: 2008-05-14 10:04:28 +0100 (Wed, 14 May 2008) $
 */
public class Object2OMElement extends Java2SimpleTypeTransformer<OMElement> {

    private OMFactory factory;

    public Object2OMElement() {
        super();
        factory = OMAbstractFactory.getOMFactory();
    }

    @Override
    protected OMElement createElement(QName element, String text, TransformationContext context) {
        OMElement omElement = AxiomHelper.createOMElement(factory, element);
        if (text == null) {
            OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            OMAttribute nil = factory.createOMAttribute("nil", xsi, "true");
            omElement.addAttribute(nil);
        } else {
            factory.createOMText(omElement, text);
        }
        return omElement;
    }

    @Override
    public Class getTargetType() {
        return OMElement.class;
    }

}
