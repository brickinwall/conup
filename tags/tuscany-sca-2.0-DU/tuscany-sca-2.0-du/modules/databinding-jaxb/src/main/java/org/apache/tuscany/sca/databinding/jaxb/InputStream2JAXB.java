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

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;

/**
 *
 * @version $Rev: 1177857 $ $Date: 2011-09-30 23:07:56 +0100 (Fri, 30 Sep 2011) $
 */
public class InputStream2JAXB extends BaseTransformer<InputStream, Object> implements
    PullTransformer<InputStream, Object> {
    private JAXBContextHelper contextHelper;
    
    public InputStream2JAXB(ExtensionPointRegistry registry) {
        contextHelper = JAXBContextHelper.getInstance(registry);
    }
    public Object transform(final InputStream source, final TransformationContext context) {
        if (source == null) {
            return null;
        }
        try {
            JAXBContext jaxbContext = contextHelper.createJAXBContext(context, false);
            StreamSource streamSource = new StreamSource(source);
            Unmarshaller unmarshaller = contextHelper.getUnmarshaller(jaxbContext);
            try {
                Object result =
                    unmarshaller.unmarshal(streamSource, JAXBContextHelper.getJavaType(context.getTargetDataType()));
                return JAXBContextHelper.createReturnValue(jaxbContext, context.getTargetDataType(), result);
            } finally {
                contextHelper.releaseJAXBUnmarshaller(jaxbContext, unmarshaller);
            }
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<InputStream> getSourceType() {
        return InputStream.class;
    }

    @Override
    protected Class<Object> getTargetType() {
        return Object.class;
    }

    @Override
    public int getWeight() {
        return 30;
    }

    @Override
    public String getTargetDataBinding() {
        return JAXBDataBinding.NAME;
    }    

}
