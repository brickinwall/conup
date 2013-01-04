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

package org.apache.tuscany.sca.interfacedef.wsdl.introspect;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.contribution.processor.ProcessorContext;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.InvalidInterfaceException;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLDefinition;
import org.apache.tuscany.sca.interfacedef.wsdl.WSDLInterface;
import org.apache.tuscany.sca.interfacedef.wsdl.xml.AbstractWSDLTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for InterfaceWSDLIntrospectorImpl.
 *
 * @version $Rev: 1035089 $ $Date: 2010-11-14 22:31:42 +0000 (Sun, 14 Nov 2010) $
 */
public class WSDLInterfaceIntrospectorTestCase extends AbstractWSDLTestCase {
    private static final QName PORTTYPE_NAME = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");

    private PortType portType;
    private WSDLDefinition definition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        URL url = getClass().getResource("../xml/stockquote.wsdl");
        ProcessorContext context = new ProcessorContext();
        definition = (WSDLDefinition)documentProcessor.read(null, new URI("stockquote.wsdl"), url, context);
        resolver.addModel(definition, context);
        definition = resolver.resolveModel(WSDLDefinition.class, definition, context);
        portType = definition.getDefinition().getPortType(PORTTYPE_NAME);
    }

    @Test
    @SuppressWarnings("unchecked")
    public final void testIntrospectPortType() throws InvalidInterfaceException {
        WSDLInterface contract = wsdlFactory.createWSDLInterface(portType, definition, resolver, null);
        Assert.assertEquals(contract.getName().getLocalPart(), "StockQuotePortType");
        List<Operation> operations = contract.getOperations();
        Assert.assertEquals(1, operations.size());
        Operation operation = operations.get(0);
        Assert.assertEquals("getLastTradePrice", operation.getName());
        DataType<List<DataType>> inputType = operation.getInputType();
        Assert.assertEquals(1, inputType.getLogical().size());
        DataType<List<DataType>> returnType = operation.getOutputType();
        Assert.assertNotNull(returnType);
        Assert.assertEquals(0, operation.getFaultTypes().size());
        // Assert.assertEquals(1,
        // operation.getWrapper().getInputChildElements().size());
        // Assert.assertEquals(1,
        // operation.getWrapper().getOutputChildElements().size());
    }

}
