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

import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sca.databinding.BaseTransformer;

/**
 * Transformer to convert data from an OMElement to XML String
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class OMElement2String extends BaseTransformer<OMElement, String> implements PullTransformer<OMElement, String> {
    // private XmlOptions options;
    
    public String transform(OMElement source, TransformationContext context) {
        try {
            StringWriter writer = new StringWriter();
            source.serialize(writer);
            return writer.toString();
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<OMElement> getSourceType() {
        return OMElement.class;
    }

    @Override
    protected Class<String> getTargetType() {
        return String.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
