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

package org.apache.tuscany.sca.interfacedef.wsdl.xml;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.wsdl.Import;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Rev: 825773 $ $Date: 2009-10-16 06:42:26 +0100 (Fri, 16 Oct 2009) $
 */
public class WSDLDocumentProcessorTestCase extends AbstractWSDLTestCase {

    @Test
    public void testWSDL() throws Exception {
        ProcessorContext context = new ProcessorContext();
        URL url = getClass().getResource("/wsdl/helloworld-service.wsdl");
        WSDLDefinition definition = (WSDLDefinition)documentProcessor.read(null, URI.create("wsdl/helloworld-service.wsdl"), url, context);
        
        Assert.assertNull(definition.getDefinition());
        Assert.assertEquals("http://helloworld", definition.getNamespace());
        URL url1 = getClass().getResource("/wsdl/helloworld-interface.wsdl");
        WSDLDefinition definition1 = (WSDLDefinition)documentProcessor.read(null, URI.create("wsdl/helloworld-interface.wsdl"), url1, context);
        Assert.assertNull(definition1.getDefinition());
        Assert.assertEquals("http://helloworld", definition1.getNamespace());

        resolver.addModel(definition, context);
        resolver.addModel(definition1, context);
        resolver.resolveModel(WSDLDefinition.class, definition, context);
        resolver.resolveModel(WSDLDefinition.class, definition1, context);
        WSDLDefinition resolved = resolver.resolveModel(WSDLDefinition.class, definition, context);
        List imports = (List)definition.getDefinition().getImports().get("http://helloworld");
        Assert.assertNotNull(imports);
        Assert.assertNotNull(((Import)imports.get(0)).getDefinition());
        Assert.assertNotNull(resolved.getDefinition().getPortType(new QName("http://helloworld", "HelloWorld")));
        Assert.assertNotNull(resolved.getDefinition().getService(new QName("http://helloworld", "HelloWorldService")));
        
        assertNotNull(resolved.getXmlSchemaType(new QName("http://greeting", "Name")));
    }

}
