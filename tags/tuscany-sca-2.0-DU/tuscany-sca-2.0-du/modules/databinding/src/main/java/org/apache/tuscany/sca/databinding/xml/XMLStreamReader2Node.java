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

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.DataPipe;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Transform DOM Node to XML XMLStreamReader
 *
 * @version $Rev: 1101170 $ $Date: 2011-05-09 20:49:09 +0100 (Mon, 09 May 2011) $
 */
public class XMLStreamReader2Node extends BaseTransformer<XMLStreamReader, Node> implements
    PullTransformer<XMLStreamReader, Node> {

    private XMLStreamReader2SAX stax2sax;
    private SAX2DOMPipe sax2domPipe;
    
    public XMLStreamReader2Node(ExtensionPointRegistry registry) {
        stax2sax = new XMLStreamReader2SAX(registry);
        sax2domPipe = new SAX2DOMPipe(registry);
    }

    public Node transform(XMLStreamReader source, TransformationContext context) {
        try {
            if (source == null) {
                return null;
            }
            DataPipe<ContentHandler, Node> pipe = sax2domPipe.newInstance();
            stax2sax.transform(source, pipe.getSink(), context);
            Node node = pipe.getResult();
            source.close();
            if (node instanceof Document) {
                Document doc = (Document)node;
                return DOMDataBinding.adjustElementName(context, doc.getDocumentElement());
            } else {
                return node;
            }
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<XMLStreamReader> getSourceType() {
        return XMLStreamReader.class;
    }

    @Override
    protected Class<Node> getTargetType() {
        return Node.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
