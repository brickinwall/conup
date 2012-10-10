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

import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.Assert;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.java.DefaultJavaInterfaceFactory;
import org.apache.tuscany.sca.interfacedef.java.JavaInterface;
import org.junit.Before;
import org.junit.Test;

import com.example.stock.async.StockExceptionTest;

public class JAXWSAsyncInterfaceProcessorTestCase {
    private ExtensionPointRegistry registry;
    
    @Before
    public void setUp() throws Exception {
        registry = new DefaultExtensionPointRegistry();
    }

    /**
     * Test method for
     * {@link org.apache.tuscany.sca.interfacedef.java.jaxws.JAXWSAsyncInterfaceProcessor#visitInterface(JavaInterface)}.
     */
    @Test
    public final void testProcessor() throws Exception {
        DefaultJavaInterfaceFactory iFactory = new DefaultJavaInterfaceFactory(registry);
        JavaInterface contract = iFactory.createJavaInterface(StockQuote.class);
        
        assertTrue(contract.isRemotable());
        
        Assert.assertEquals(1,contract.getOperations().size());
        
        List<Operation> asyncOperations = (List<Operation>) contract.getAttributes().get("JAXWS-ASYNC-OPERATIONS");
        Assert.assertEquals(2,asyncOperations.size());
        
        //list operation
        System.out.println(">>> Filtered Operations");
        for(Operation o : contract.getOperations()) {
            System.out.println(">>>>>>" + o);
        }
        
    }
    
    @Test
    public final void testProcessorGenerated() throws Exception {
        DefaultJavaInterfaceFactory iFactory = new DefaultJavaInterfaceFactory(registry);
        JavaInterface contract = iFactory.createJavaInterface(StockExceptionTest.class);
        
        assertTrue(contract.isRemotable());
        
        Assert.assertEquals(1,contract.getOperations().size());
        
        List<Operation> asyncOperations = (List<Operation>) contract.getAttributes().get("JAXWS-ASYNC-OPERATIONS");
        Assert.assertEquals(2,asyncOperations.size());
        
        //list operation
        System.out.println(">>> Filtered Operations");
        for(Operation o : contract.getOperations()) {
            System.out.println(">>>>>>" + o);
        }
        
    }
}
