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

package org.apache.tuscany.sca.host.rmi;

import java.rmi.Remote;


/**
 * Default implementation of an extensible RMI host.
 *
 * @version $Rev: 921690 $ $Date: 2010-03-11 04:45:13 +0000 (Thu, 11 Mar 2010) $
 */
public class ExtensibleRMIHost implements RMIHost {

    private RMIHostExtensionPoint rmiHosts;

    public ExtensibleRMIHost(RMIHostExtensionPoint rmiHosts) {
        this.rmiHosts = rmiHosts;
    }

    public String registerService(String uri, Remote serviceObject) throws RMIHostException, RMIHostRuntimeException {
        if (rmiHosts.getRMIHosts().isEmpty()) {
            throw new RMIHostException("No RMI host is available");
        }
        return getDefaultHost().registerService(uri, serviceObject);
    }

    public void unregisterService(String uri) throws RMIHostException, RMIHostRuntimeException {
        if (rmiHosts.getRMIHosts().isEmpty()) {
            throw new RMIHostException("No RMI host is available");
        }
        getDefaultHost().unregisterService(uri);
    }

    public Remote findService(String uri) throws RMIHostException, RMIHostRuntimeException {
        if (rmiHosts.getRMIHosts().isEmpty()) {
            throw new RMIHostException("No RMI host available");
        }
        return getDefaultHost().findService(uri);
    }

    protected RMIHost getDefaultHost() {
        return rmiHosts.getRMIHosts().get(0);
    }
}
