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

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.xml.BeanXMLStreamReaderImpl;
import org.apache.tuscany.sca.interfacedef.util.XMLType;

/**
 * Transformer to convert data from a JavaBean object to XMLStreamReader.
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class JavaBean2XMLStreamReaderTransformer extends BaseTransformer<Object, XMLStreamReader> implements
    PullTransformer<Object, XMLStreamReader> {

    public XMLStreamReader transform(Object source, TransformationContext context) {
        try {
            javax.xml.namespace.QName name = null;
            if (context != null) {
                Object logical = context.getSourceDataType().getLogical();
                if (logical instanceof XMLType) {
                    name = ((XMLType)logical).getElementName();
                }
            }
            return new BeanXMLStreamReaderImpl(name, source);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<Object> getSourceType() {
        return Object.class;
    }

    @Override
    protected Class<XMLStreamReader> getTargetType() {
        return XMLStreamReader.class;
    }

    @Override
    public int getWeight() {
        return 50;
    }

}
