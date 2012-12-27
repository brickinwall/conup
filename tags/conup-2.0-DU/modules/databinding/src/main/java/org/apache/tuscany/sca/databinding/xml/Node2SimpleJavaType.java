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

import org.apache.tuscany.sca.databinding.impl.SimpleType2JavaTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Transformer to convert data from a simple java Object to OMElement.
 *
 * @version $Rev: 656146 $ $Date: 2008-05-14 09:22:08 +0100 (Wed, 14 May 2008) $
 */
public class Node2SimpleJavaType extends SimpleType2JavaTransformer<Node> {

    @Override
    protected String getText(Node source) {
        if (source instanceof Document) {
            source = ((Document)source).getDocumentElement();
        }
        return source.getTextContent();

    }

    @Override
    public Class getSourceType() {
        return Node.class;
    }
}
