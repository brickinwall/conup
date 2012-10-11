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
package org.apache.tuscany.sca.databinding.sdo2om;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.sdo.SDOContextHelper;

import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

/**
 * SDO XMLDocument --> AXIOM OMElement transformer
 * @version $Rev: 928835 $ $Date: 2010-03-29 18:04:35 +0100 (Mon, 29 Mar 2010) $
 */
public class XMLDocument2OMElement extends BaseTransformer<XMLDocument, OMElement> implements
    PullTransformer<XMLDocument, OMElement> {

    public OMElement transform(XMLDocument source, TransformationContext context) {
        HelperContext helperContext = SDOContextHelper.getHelperContext(context, true);
        SDODataSource dataSource = new SDODataSource(source, helperContext);
        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName name = new QName(source.getRootElementURI(), source.getRootElementName());
        OMElement element = AxiomHelper.createOMElement(factory, name, dataSource);
        return element;
    }

    @Override
    protected Class<XMLDocument> getSourceType() {
        return XMLDocument.class;
    }

    @Override
    protected Class<OMElement> getTargetType() {
        return OMElement.class;
    }

    @Override
    public int getWeight() {
        return 10;
    }

}
