/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tuscany.sca.implementation.bpel.ode;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Geronimo transaction factory
 * 
 * @version $Rev: 639620 $ $Date: 2008-03-21 13:09:34 +0000 (Fri, 21 Mar 2008) $
 */
public class GeronimoTxFactory {
    private static final Log __log = LogFactory.getLog(GeronimoTxFactory.class);

    /* Public no-arg constructor is required */
    public GeronimoTxFactory() {
    }

    public TransactionManager getTransactionManager() {
        __log.info("Using embedded Geronimo transaction manager");
        try {
            Object obj = new org.apache.geronimo.transaction.manager.GeronimoTransactionManager();
            return (TransactionManager) obj;
        } catch (Exception except) {
            throw new IllegalStateException("Unable to instantiate Geronimo Transaction Manager", except);
        }
    }
}
