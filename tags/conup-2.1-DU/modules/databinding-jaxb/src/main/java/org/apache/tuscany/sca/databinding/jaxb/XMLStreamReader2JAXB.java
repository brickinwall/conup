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
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;

/**
 *
 * @version $Rev: 1177857 $ $Date: 2011-09-30 23:07:56 +0100 (Fri, 30 Sep 2011) $
 */
public class XMLStreamReader2JAXB extends BaseTransformer<XMLStreamReader, Object> implements
    PullTransformer<XMLStreamReader, Object> {

    private JAXBContextHelper contextHelper;

    public XMLStreamReader2JAXB(ExtensionPointRegistry registry) {
        contextHelper = JAXBContextHelper.getInstance(registry);
    }

    public Object transform(XMLStreamReader source, TransformationContext context) {
        if (source == null) {
            return null;
        }
        try {
            JAXBContext jaxbContext = contextHelper.createJAXBContext(context, false);
            Unmarshaller unmarshaller = contextHelper.getUnmarshaller(jaxbContext);
            try {
                // FIXME: [rfeng] If the java type is Object.class, the unmarshalled result will be
                // a DOM Node
                Object result =
                    unmarshaller.unmarshal(source, JAXBContextHelper.getJavaType(context.getTargetDataType()));
                source.close();
                return JAXBContextHelper.createReturnValue(jaxbContext, context.getTargetDataType(), result);
            } finally {
                contextHelper.releaseJAXBUnmarshaller(jaxbContext, unmarshaller);
            }

        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public Class<XMLStreamReader> getSourceType() {
        return XMLStreamReader.class;
    }

    @Override
    public Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public int getWeight() {
        return 10;
    }

    @Override
    public String getTargetDataBinding() {
        return JAXBDataBinding.NAME;
    }
}
