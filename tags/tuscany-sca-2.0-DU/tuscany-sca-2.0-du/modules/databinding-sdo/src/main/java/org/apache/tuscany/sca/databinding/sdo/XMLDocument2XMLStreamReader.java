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
package org.apache.tuscany.sca.databinding.sdo;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.apache.tuscany.sdo.api.SDOUtil;
import org.apache.tuscany.sdo.api.XMLStreamHelper;

import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XMLDocument;

public class XMLDocument2XMLStreamReader extends BaseTransformer<XMLDocument, XMLStreamReader> implements
    PullTransformer<XMLDocument, XMLStreamReader> {
    /**
     * @param source
     * @param context
     * @return
     */
    public XMLStreamReader transform(XMLDocument source, TransformationContext context) {
        if (source == null) {
            return null;
        }
        try {
            HelperContext helperContext = SDOContextHelper.getHelperContext(context, true);
            XMLStreamHelper streamHelper = SDOUtil.createXMLStreamHelper(helperContext);
            return streamHelper.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<XMLDocument> getSourceType() {
        return XMLDocument.class;
    }

    @Override
    protected Class<XMLStreamReader> getTargetType() {
        return XMLStreamReader.class;
    }

    @Override
    public int getWeight() {
        return 10;
    }

}
