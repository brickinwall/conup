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

package org.apache.tuscany.sca.databinding.json.axiom;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.Transformer;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.json.JSONDataBinding;
import org.apache.tuscany.sca.databinding.json.JSONHelper;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.codehaus.jettison.json.JSONObject;
import org.oasisopen.sca.annotation.Service;

/**
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
@Service(Transformer.class)
public class JSON2OMElement extends BaseTransformer<Object, OMElement> implements PullTransformer<Object, OMElement> {

    private OMFactory factory = OMAbstractFactory.getOMFactory();

    @Override
    protected Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    protected Class<OMElement> getTargetType() {
        return OMElement.class;
    }

    public OMElement transform(Object source, TransformationContext context) {
        try {
            JSONObject json = JSONHelper.toJettison(source);
            if (json == null) {
                return null;
            }
            String ns = JSONDataBinding.ROOT_ELEMENT.getNamespaceURI();
            String name = JSONDataBinding.ROOT_ELEMENT.getLocalPart();
            if (context != null) {
                DataType<?> dataType = context.getTargetDataType();
                Object logical = dataType.getLogical();
                if (logical instanceof XMLType) {
                    XMLType xmlType = (XMLType)logical;
                    if (xmlType.isElement()) {
                        ns = xmlType.getElementName().getNamespaceURI();
                        name = xmlType.getElementName().getLocalPart();
                    }
                }
            }
            JSONBadgerfishDataSource ds = new JSONBadgerfishDataSource(json);
            OMNamespace namespace = factory.createOMNamespace(ns, "");
            return factory.createOMElement(ds, name, namespace);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public int getWeight() {
        return 500;
    }

    @Override
    public String getSourceDataBinding() {
        return JSONDataBinding.NAME;
    }

}
