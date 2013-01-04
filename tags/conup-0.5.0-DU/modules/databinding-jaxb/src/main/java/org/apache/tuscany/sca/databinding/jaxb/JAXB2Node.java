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
package org.apache.tuscany.sca.databinding.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.tuscany.sca.common.xml.dom.DOMHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.xml.DOMDataBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @version $Rev: 1177857 $ $Date: 2011-09-30 23:07:56 +0100 (Fri, 30 Sep 2011) $
 */
public class JAXB2Node extends BaseTransformer<Object, Node> implements PullTransformer<Object, Node> {
    private DOMHelper helper;
    private JAXBContextHelper contextHelper;

    public JAXB2Node(ExtensionPointRegistry registry) {
        super();
        helper = DOMHelper.getInstance(registry);
        contextHelper = JAXBContextHelper.getInstance(registry);
    }

    public Node transform(Object source, TransformationContext tContext) {
        //        if (source == null) {
        //            return null;
        //        }
        try {
            JAXBContext context = contextHelper.createJAXBContext(tContext, true);

            // FIXME: The default Marshaller doesn't support
            // marshaller.getNode()
            Document document = helper.newDocument();
            Object jaxbElement = JAXBContextHelper.createJAXBElement(context, tContext.getSourceDataType(), source);
            Marshaller marshaller = contextHelper.getMarshaller(context);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);

            try {
                marshaller.marshal(jaxbElement, document);
            } finally {
                contextHelper.releaseJAXBMarshaller(context, marshaller);
            }
            return DOMDataBinding.adjustElementName(tContext, document.getDocumentElement());
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    protected Class<Node> getTargetType() {
        return Node.class;
    }

    @Override
    public int getWeight() {
        return 30;
    }

    @Override
    public String getSourceDataBinding() {
        return JAXBDataBinding.NAME;
    }
}
