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
package org.apache.tuscany.sca.core.invocation.impl;

import static org.junit.Assert.assertEquals;

import org.apache.tuscany.sca.core.DefaultExtensionPointRegistry;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.OperationImpl;
import org.apache.tuscany.sca.invocation.Interceptor;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.Phase;
import org.junit.Test;

/**
 * @version $Rev: 1041866 $ $Date: 2010-12-03 23:22:31 +0800 (周五, 03 十二月 2010) $
 */
public class InvocationChainImplTestCase {

    @Test
    public void testInsertAtEnd() throws Exception {
        Operation op = newOperation("foo");
        InvocationChain chain = new InvocationChainImpl(op, op, true, new PhaseManager(new DefaultExtensionPointRegistry()), false);
        Interceptor inter2 = new MockInterceptor();
        Interceptor inter1 = new MockInterceptor();
        chain.addInterceptor(inter1);
        chain.addInterceptor(inter2);
        Interceptor head = (Interceptor)chain.getHeadInvoker();
        assertEquals(inter1, head);
        assertEquals(inter2, head.getNext());
    }

    @Test
    public void testAddByPhase() throws Exception {
        Operation op = newOperation("foo");
        InvocationChain chain = new InvocationChainImpl(op, op, false, new PhaseManager(new DefaultExtensionPointRegistry()), false);
        Interceptor inter1 = new MockInterceptor();
        Interceptor inter2 = new MockInterceptor();
        Interceptor inter3 = new MockInterceptor();
        Interceptor inter4 = new MockInterceptor();
        chain.addInterceptor(inter3); // SERVICE
        chain.addInterceptor(Phase.IMPLEMENTATION_POLICY, inter4);
        chain.addInterceptor(Phase.SERVICE_POLICY, inter2);
        chain.addInterceptor(Phase.SERVICE_BINDING, inter1);
        Interceptor head = (Interceptor)chain.getHeadInvoker();
        assertEquals(inter1, head);
        assertEquals(inter2, inter1.getNext());
        assertEquals(inter3, inter2.getNext());
        assertEquals(inter4, inter3.getNext());
    }

    private class MockInterceptor implements Interceptor {

        private Invoker next;

        public Message invoke(Message msg) {
            return null;
        }

        public void setNext(Invoker next) {
            this.next = next;
        }

        public Invoker getNext() {
            return next;
        }

    }

    private static Operation newOperation(String name) {
        Operation operation = new OperationImpl();
        operation.setName(name);
        return operation;
    }
}
