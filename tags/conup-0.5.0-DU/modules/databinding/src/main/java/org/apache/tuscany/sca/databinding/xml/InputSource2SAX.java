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

import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PushTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Push InputSource to SAX
 *
 * @version $Rev: 916888 $ $Date: 2010-02-27 00:44:05 +0000 (Sat, 27 Feb 2010) $
 */
public class InputSource2SAX extends BaseTransformer<InputSource, ContentHandler> implements
    PushTransformer<InputSource, ContentHandler> {

    public void transform(InputSource source, ContentHandler target, TransformationContext context) {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            reader.setContentHandler(target);
            reader.parse(source);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public Class<InputSource> getSourceType() {
        return InputSource.class;
    }

    @Override
    public Class<ContentHandler> getTargetType() {
        return ContentHandler.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
