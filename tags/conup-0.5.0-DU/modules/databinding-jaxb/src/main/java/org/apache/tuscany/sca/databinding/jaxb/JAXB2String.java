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

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.xml.XMLStringDataBinding;

/**
 *
 * @version $Rev: 1177857 $ $Date: 2011-09-30 23:07:56 +0100 (Fri, 30 Sep 2011) $
 */
public class JAXB2String extends BaseTransformer<Object, String> implements PullTransformer<Object, String> {
    private JAXBContextHelper contextHelper;
    
    public JAXB2String(ExtensionPointRegistry registry) {
        contextHelper = JAXBContextHelper.getInstance(registry);
    }
    public String transform(Object source, TransformationContext tContext) {
        try {
            JAXBContext context = contextHelper.createJAXBContext(tContext, true);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Object jaxbElement = JAXBContextHelper.createJAXBElement(context, tContext.getSourceDataType(), source);
            Marshaller marshaller = contextHelper.getMarshaller(context);
            try {
                marshaller.marshal(jaxbElement, result);
            } finally {
                contextHelper.releaseJAXBMarshaller(context, marshaller);
            }
            return writer.toString();
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    protected Class<String> getTargetType() {
        return String.class;
    }

    @Override
    public int getWeight() {
        return 30;
    }

    @Override
    public String getSourceDataBinding() {
        return JAXBDataBinding.NAME;
    }

    @Override
    public String getTargetDataBinding() {
        return XMLStringDataBinding.NAME;
    }
}
