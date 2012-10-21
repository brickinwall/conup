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

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.BaseTransformer;

/**
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class XMLStreamReader2OMElement extends BaseTransformer<XMLStreamReader, OMElement> implements
    PullTransformer<XMLStreamReader, OMElement> {

    public XMLStreamReader2OMElement() {
        super();
    }

    public OMElement transform(XMLStreamReader source, TransformationContext context) {
        if (source == null) {
            return null;
        }
        try {
            StAXOMBuilder builder = new StAXOMBuilder(source);
            OMElement element = builder.getDocumentElement();
            AxiomHelper.adjustElementName(context, element);
            return element;
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<OMElement> getTargetType() {
        return OMElement.class;
    }

    @Override
    protected Class<XMLStreamReader> getSourceType() {
        return XMLStreamReader.class;
    }

    @Override
    public int getWeight() {
        return 10;
    }

}
