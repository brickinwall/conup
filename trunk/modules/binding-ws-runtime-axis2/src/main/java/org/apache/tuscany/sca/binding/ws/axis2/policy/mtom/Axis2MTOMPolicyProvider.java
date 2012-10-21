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

package org.apache.tuscany.sca.binding.ws.axis2.policy.mtom;

import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.tuscany.sca.policy.PolicySubject;
import org.apache.tuscany.sca.provider.BasePolicyProvider;

/**
 * @version $Rev: 906175 $ $Date: 2010-02-03 19:02:33 +0000 (Wed, 03 Feb 2010) $
 */
public class Axis2MTOMPolicyProvider extends BasePolicyProvider<Object> {

    public Axis2MTOMPolicyProvider(PolicySubject subject) {
        super(Object.class, subject);
    }

    public void configureBinding(Object configuration) {
        
        if (configuration instanceof ConfigurationContext){
            ConfigurationContext configurationContext = (ConfigurationContext)configuration;
            configurationContext.getAxisConfiguration().getParameter(Configuration.ENABLE_MTOM).setLocked(false);
            configurationContext.getAxisConfiguration().getParameter(Configuration.ENABLE_MTOM).setValue("true");
        }
    }
}
