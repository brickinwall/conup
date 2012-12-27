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

package org.apache.tuscany.sca.binding.jms.host;

import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.apache.tuscany.sca.binding.jms.JMSBindingException;
import org.apache.tuscany.sca.binding.jms.provider.JMSBindingServiceBindingProvider;
import org.apache.tuscany.sca.binding.jms.provider.JMSResourceFactory;
import org.apache.tuscany.sca.runtime.RuntimeComponentService;
import org.apache.tuscany.sca.work.WorkScheduler;

public class DefaultJMSServiceListenerFactory implements JMSServiceListenerFactory {

    private WorkScheduler workScheduler;

    public DefaultJMSServiceListenerFactory(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    public JMSServiceListener createJMSServiceListener(JMSServiceListenerDetails jmsSLD) {
        try {

            JMSResourceFactory rf = ((JMSBindingServiceBindingProvider)jmsSLD).getResourceFactory();
            
            RuntimeComponentService service = (RuntimeComponentService) jmsSLD.getEndpoint().getService();
            MessageListener listener = new DefaultServiceInvoker(jmsSLD.getEndpoint(), jmsSLD.getTargetBinding(), jmsSLD.getMessageFactory(), rf);
           
            return new DefaultJMSServiceListener(listener, service.getName(), service.isForCallback(), jmsSLD.getJmsBinding(), workScheduler, rf);

        } catch (NamingException e) {
            throw new JMSBindingException(e);
        }
    }
}
