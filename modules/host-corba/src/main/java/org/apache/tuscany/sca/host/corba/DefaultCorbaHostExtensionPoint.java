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

package org.apache.tuscany.sca.host.corba;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;
import org.apache.tuscany.sca.extensibility.ServiceHelper;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * @version $Rev: 924158 $ $Date: 2010-03-17 05:21:11 +0000 (Wed, 17 Mar 2010) $
 * Default implementation of CorbaHostExtensionPoint
 */
public class DefaultCorbaHostExtensionPoint implements CorbaHostExtensionPoint, LifeCycleListener {
    private ExtensionPointRegistry registry;
    private boolean loaded;
    private List<CorbaHost> corbaHosts = new ArrayList<CorbaHost>();

    public DefaultCorbaHostExtensionPoint(ExtensionPointRegistry registry) {
        this.registry = registry;
    }

    private synchronized void loadHosts() {
        if (loaded) {
            return;
        }
        try {
            ServiceDeclaration sd = registry.getServiceDiscovery().getServiceDeclaration(CorbaHost.class);
            CorbaHost host = ServiceHelper.newInstance(registry, sd);
            ServiceHelper.start(host);
            corbaHosts.add(host);
        } catch (Throwable e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void addCorbaHost(CorbaHost host) {
        corbaHosts.add(host);
    }

    public void removeCorbaHost(CorbaHost host) {
        corbaHosts.remove(host);
    }

    public List<CorbaHost> getCorbaHosts() {
        loadHosts();
        return corbaHosts;
    }

    public void start() {
    }

    public void stop() {
        ServiceHelper.stop(corbaHosts);
        corbaHosts.clear();
        loaded = false;
    }

}
