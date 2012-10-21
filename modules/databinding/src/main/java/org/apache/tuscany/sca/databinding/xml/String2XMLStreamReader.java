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
package org.apache.tuscany.sca.databinding.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;

public class String2XMLStreamReader extends BaseTransformer<String, XMLStreamReader> implements
    PullTransformer<String, XMLStreamReader> {
    private StAXHelper helper;
    
    public String2XMLStreamReader(ExtensionPointRegistry registry) {
        helper = StAXHelper.getInstance(registry);
    }
    
    public XMLStreamReader transform(String source, TransformationContext context) {
        try {
            return helper.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<String> getSourceType() {
        return String.class;
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
