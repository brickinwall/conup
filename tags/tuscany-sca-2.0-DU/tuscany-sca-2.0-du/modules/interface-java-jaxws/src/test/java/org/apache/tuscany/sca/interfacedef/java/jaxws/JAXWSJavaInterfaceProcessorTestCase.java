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

package org.apache.tuscany.sca.interfacedef.java.jaxws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.example.stock.StockExceptionTest;

/**
 *
 * @version $Rev: 1213702 $ $Date: 2011-12-13 14:12:38 +0000 (Tue, 13 Dec 2011) $
 */
public class JAXWSJavaInterfaceProcessorTestCase {
    private ExtensionPointRegistry registry;
    // private JAXWSJavaInterfaceProcessor interfaceProcessor;

    @Before
    public void setUp() throws Exception {
        registry = new DefaultExtensionPointRegistry();
//        DataBindingExtensionPoint db = new DefaultDataBindingExtensionPoint(registry);
//        XMLAdapterExtensionPoint xa = new DefaultXMLAdapterExtensionPoint();
        // interfaceProcessor = new JAXWSJavaInterfaceProcessor(db, new JAXWSFaultExceptionMapper(db, xa), xa);
    }

    @Test
    public void testWrapper() throws Exception {
        DefaultJavaInterfaceFactory iFactory = new DefaultJavaInterfaceFactory(registry);
        JavaInterface contract = iFactory.createJavaInterface(StockExceptionTest.class);

        // interfaceProcessor.visitInterface(contract);
        Operation op = contract.getOperations().get(0);
        Assert.assertTrue(!op.isInputWrapperStyle());
        Assert.assertEquals(new QName("http://www.example.com/stock", "stockQuoteOffer"), op.getInputWrapper().getWrapperElement().getQName());
        Assert.assertEquals(new QName("http://www.example.com/stock", "stockQuoteOfferResponse"), op.getOutputWrapper().getWrapperElement().getQName());
    }

    /**
     * Test method for
     * {@link org.apache.tuscany.sca.interfacedef.java.jaxws.JAXWSJavaInterfaceProcessor#visitInterface(JavaInterface)}.
     */
    @Test
    public final void testProcessor() throws Exception {
        DefaultJavaInterfaceFactory iFactory = new DefaultJavaInterfaceFactory(registry);
        JavaInterface contract = iFactory.createJavaInterface(WebServiceInterfaceWithoutAnnotation.class);

        // interfaceProcessor.visitInterface(contract);
        assertFalse(contract.isRemotable());

        contract = iFactory.createJavaInterface(WebServiceInterfaceWithAnnotation.class);
        // interfaceProcessor.visitInterface(contract);
        assertTrue(contract.isRemotable());

        Operation op1 = contract.getOperations().get(0);
        Operation op2 = contract.getOperations().get(1);

        Operation op = null;
        if ("m1".equals(op1.getName())) {
            op = op1;
        } else {
            op = op2;
        }

        assertTrue(!op.isInputWrapperStyle() && op.getInputWrapper() == null);

        if ("M2".equals(op2.getName())) {
            op = op2;
        } else {
            op = op1;
        }
        assertTrue(!op.isInputWrapperStyle() && op.getInputWrapper() != null);

    }

    @WebService
    private static interface WebServiceInterfaceWithAnnotation {

        @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
        @WebMethod(operationName = "m1")
        String m1(String str);

        @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
        @WebMethod(operationName = "M2")
        String m2(String str, int i);
    }

    private static interface WebServiceInterfaceWithoutAnnotation {

    }
}
