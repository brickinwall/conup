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

package org.apache.tuscany.sca.policy.transaction.impl;

import javax.xml.namespace.QName;

import org.apache.tuscany.sca.policy.transaction.TransactionPolicy;

/**
 * @version $Rev: 918831 $ $Date: 2010-03-04 03:05:04 +0000 (Thu, 04 Mar 2010) $
 */
public class TransactionPolicyImpl implements TransactionPolicy {
    private boolean unresolved;
    private int transactionTimeout = 1200;
    private Action action = Action.PROPAGATE;

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action policy) {
        this.action = policy;
    }

    public QName getSchemaName() {
        return NAME;
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }
}
