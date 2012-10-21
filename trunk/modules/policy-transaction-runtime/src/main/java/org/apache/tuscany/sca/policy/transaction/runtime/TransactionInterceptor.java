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

package org.apache.tuscany.sca.policy.transaction.runtime;

import java.util.logging.Logger;

import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.PhasedInterceptor;
import org.apache.tuscany.sca.policy.transaction.TransactionPolicy;

/**
 * @version $Rev: 1175722 $ $Date: 2011-09-26 09:59:10 +0100 (Mon, 26 Sep 2011) $
 */
public class TransactionInterceptor implements PhasedInterceptor {
    private static final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());
    
    private Invoker next;
    private TransactionManagerHelper helper;
    private boolean outbound;
    private TransactionPolicy interactionPolicy;
    private TransactionPolicy implementationPolicy;
    private String phase;

    public TransactionInterceptor(TransactionManagerHelper helper,
                                  boolean outbound,
                                  TransactionPolicy interactionPolicy,
                                  TransactionPolicy implementationPolicy,
                                  String phase) {
        super();
        this.helper = helper;
        this.outbound = outbound;
        this.interactionPolicy = interactionPolicy;
        this.implementationPolicy = implementationPolicy;
        this.phase = phase;
    }

    /**
     * @see org.apache.tuscany.sca.invocation.Interceptor#getNext()
     */
    public Invoker getNext() {
        return next;
    }

    /**
     * @see org.apache.tuscany.sca.invocation.Interceptor#setNext(org.apache.tuscany.sca.invocation.Invoker)
     */
    public void setNext(Invoker next) {
        this.next = next;
    }

    /**
     * @see org.apache.tuscany.sca.invocation.Invoker#invoke(org.apache.tuscany.sca.invocation.Message)
     */
    public Message invoke(Message msg) {
        //logger.info("Executing TransactionInterceptor.invoke");
        TransactionalInvocation invocation = new TransactionalInvocation(next, msg);

        Message result = null;
        if (msg.getOperation().isNonBlocking()) {

        }
        
        // initialize default values
        TransactionIntent interactionIntent = null; //TransactionIntent.propagatesTransacton;
        TransactionIntent implementationIntent = TransactionIntent.managedTransactionGlobal;
        
        if (interactionPolicy != null) {
            if (interactionPolicy.getAction() == TransactionPolicy.Action.PROPAGATE) {
                interactionIntent = TransactionIntent.propagatesTransacton;
            } else if (interactionPolicy.getAction() == TransactionPolicy.Action.REQUIRE_NONE) {
                interactionIntent = TransactionIntent.suspendsTransaction;
                if(implementationPolicy == null) {
                    implementationIntent = TransactionIntent.noManagedTransaction;
                }
            } else {
                interactionIntent = TransactionIntent.suspendsTransaction;
            }
        }
        
        if (implementationPolicy != null) {
            switch (implementationPolicy.getAction()) {
                case REQUIRE_GLOBAL:
                    implementationIntent = TransactionIntent.managedTransactionGlobal;
                    break;
                case REQUIRE_LOCAL:
                    implementationIntent = TransactionIntent.managedTransactionLocal;
                    break;
                default:
                    implementationIntent = TransactionIntent.noManagedTransaction;
                    break;
            }
        }
        try {
            if (outbound) {
                result = helper.handlesOutbound(interactionIntent, implementationIntent, invocation);
            } else {
                result = helper.handlesInbound(interactionIntent, implementationIntent, invocation);
            }
            
        } catch (Throwable e) {
        	  if (e instanceof Error) {
                  throw (Error)e;
              } else if (e instanceof RuntimeException) {
                  throw (RuntimeException)e;
              } else {
                  result = msg;
                  msg.setFaultBody(e);
              }
           
        }
        return result;
    }

    private static class TransactionalInvocation implements TransactionalAction<Message> {
        private final Invoker invoker;
        private final Message message;

        public TransactionalInvocation(Invoker invoker, Message message) {
            super();
            this.invoker = invoker;
            this.message = message;
        }

        public Message run() throws Exception {
            return invoker.invoke(message);
        }

    }
    
    public String getPhase() {
        return phase;
    }

}
