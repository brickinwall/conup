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

import javax.xml.stream.XMLStreamReader;

import org.apache.tuscany.sca.common.xml.stax.StAXHelper;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PushTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.xml.sax.ContentHandler;

/**
 * XMLStreamReader to SAX events
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class XMLStreamReader2SAX extends BaseTransformer<XMLStreamReader, ContentHandler> implements
    PushTransformer<XMLStreamReader, ContentHandler> {
    
    private StAXHelper helper;
    
    public XMLStreamReader2SAX(ExtensionPointRegistry registry) {
        helper = StAXHelper.getInstance(registry);
    }
    
    @Override
    protected Class<ContentHandler> getTargetType() {
        return ContentHandler.class;
    }

    @Override
    protected Class<XMLStreamReader> getSourceType() {
        return XMLStreamReader.class;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.PushTransformer#getWeight()
     */
    @Override
    public int getWeight() {
        return 20;
    }

    /**
     * @see org.apache.tuscany.sca.databinding.PushTransformer#transform(java.lang.Object,
     *      java.lang.Object,
     *      org.apache.tuscany.sca.databinding.TransformationContext)
     */
    public void transform(XMLStreamReader source, ContentHandler sink, TransformationContext context) {
        try {
            helper.saveAsSAX(source, sink);
            source.close();
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

}
