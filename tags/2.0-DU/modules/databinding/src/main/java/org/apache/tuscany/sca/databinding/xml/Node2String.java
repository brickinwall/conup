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

import java.io.StringWriter;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.databinding.BaseTransformer;
import org.apache.tuscany.sca.databinding.PullTransformer;
import org.apache.tuscany.sca.databinding.TransformationContext;
import org.apache.tuscany.sca.databinding.TransformationException;
import org.w3c.dom.Node;

/**
 * Transform DOM Node to XML String
 *
 * @version $Rev: 945980 $ $Date: 2010-05-19 01:53:52 +0100 (Wed, 19 May 2010) $
 */
public class Node2String extends BaseTransformer<Node, String> implements PullTransformer<Node, String> {
    private final Node2Writer TRANSFORMER;
    
    public Node2String(ExtensionPointRegistry registry) {
        super();
        this.TRANSFORMER = new Node2Writer(registry);
    }
    
    public String transform(Node source, TransformationContext context) {
        try {
            StringWriter writer = new StringWriter();
            TRANSFORMER.transform(source, writer, context);
            return writer.toString();
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    @Override
    public Class<Node> getSourceType() {
        return Node.class;
    }

    @Override
    public Class<String> getTargetType() {
        return String.class;
    }

    @Override
    public int getWeight() {
        return 40;
    }

}
