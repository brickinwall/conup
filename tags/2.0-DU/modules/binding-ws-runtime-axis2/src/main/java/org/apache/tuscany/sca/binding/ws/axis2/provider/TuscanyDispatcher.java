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

package org.apache.tuscany.sca.binding.ws.axis2.provider;

import java.net.URI;
import java.util.HashMap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.RequestURIBasedDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Tuscany specific Axis2 Dispatcher that enables using services
 * exposed at the SCA defined service URI instead of /services/<serviceName> 
 *
 * @version $Rev: 917502 $ $Date: 2010-03-01 12:54:07 +0000 (Mon, 01 Mar 2010) $
 */
public class TuscanyDispatcher extends RequestURIBasedDispatcher {

    public static final String NAME = "TuscanyDispatcher";
    private static final Log log = LogFactory.getLog(RequestURIBasedDispatcher.class);
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();

        if (toEPR != null) {
            if(isDebugEnabled){
                log.debug("Checking for Service using target endpoint address : " + toEPR.getAddress());
            }

            String path = URI.create(toEPR.getAddress()).getPath();
            
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();
            AxisConfiguration registry = configurationContext.getAxisConfiguration();

            String serviceName = findAxisServiceName(registry, path);
            return registry.getService(serviceName);

        } else {
            if(isDebugEnabled){
                log.debug("Attempted to check for Service using null target endpoint URI");
            }
            return null;
        }
    }

    @Override
    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    protected String findAxisServiceName(AxisConfiguration registry, String path) {
        HashMap services = registry.getServices();
        if (services == null) {
            return null;
        }
        String[] parts = path.split("/");
        String serviceName = "";
        for (int i=parts.length-1; i>=0; i--) {
            serviceName = parts[i] + serviceName;
            if (services.containsKey(serviceName)) {
                return serviceName;
            }
            serviceName = "/" + serviceName;
            if (services.containsKey(serviceName)) {
                return serviceName;
            }
        }

        return null;
    }

}
