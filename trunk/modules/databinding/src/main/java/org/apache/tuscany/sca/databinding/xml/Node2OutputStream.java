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

import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PushTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.w3c.dom.Node;

/**
 * Push DOM Node to OutputStream
 *
 * @version $Rev: 945980 $ $Date: 2010-05-19 01:53:52 +0100 (Wed, 19 May 2010) $
 */
public class Node2OutputStream extends BaseTransformer<Node, OutputStream> implements
    PushTransformer<Node, OutputStream> {

    private final Source2ResultTransformer TRANSFORMER;
    
    public Node2OutputStream(ExtensionPointRegistry registry) {
        super();
        this.TRANSFORMER = new Source2ResultTransformer(registry);
    }
    
    public void transform(Node source, OutputStream writer, TransformationContext context) {
        try {
            Source domSource = new DOMSource(source);
            Result result = new StreamResult(writer);
            TRANSFORMER.transform(domSource, result, context);
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    protected Class<Node> getSourceType() {
        return Node.class;
    }

    @Override
    protected Class<OutputStream> getTargetType() {
        return OutputStream.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
