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

package org.apache.tuscany.sca.interfacedef.impl;


import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev: 802989 $ $Date: 2009-08-11 06:14:38 +0100 (Tue, 11 Aug 2009) $
 */
public class InterfaceContractTestCase {
    private InterfaceContract contract;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        contract = new MockInterfaceContract();
        Interface i1 = new MockInterface();
        contract.setInterface(i1);
        Operation op1 = newOperation("op1");
        i1.getOperations().add(op1);
        Interface i2 = new MockInterface();
        contract.setCallbackInterface(i2);
        Operation callbackOp1 = newOperation("callbackOp1");
        i2.getOperations().add(callbackOp1);
    }
    
    @Test
    public void testClone() throws Exception {
        InterfaceContract copy = (InterfaceContract) contract.clone();
        Assert.assertNotNull(copy);
        Assert.assertNotSame(copy.getCallbackInterface(), contract.getCallbackInterface());
        Assert.assertNotSame(copy.getInterface(), contract.getInterface());
    }

    private static class MockInterfaceContract extends InterfaceContractImpl implements InterfaceContract {
    }

    private static class MockInterface extends InterfaceImpl implements Interface {
    }

    private static Operation newOperation(String name) {
        Operation operation = new OperationImpl();
        operation.setName(name);
        return operation;
    }
}
