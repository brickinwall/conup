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
 * RMI Service hosting interface to be implemented by host environments that allows SCA Components
 * to register RMI Services to handle inbound service requests over RMI to SCA Components
 *
 * @version $Rev: 921690 $ $Date: 2010-03-11 04:45:13 +0000 (Thu, 11 Mar 2010) $
 */
public interface RMIHost {
    int RMI_DEFAULT_PORT = 1099;

    /**
     * Register an RMI service with the given name and port
     *
     * @param uri the URI against which the server is to be registered
     * @param serviceObject the server object to be registered
     * @return TODO
     * @throws RMIHostException
     * @throws RMIHostRuntimeException
     */
    String registerService(String uri, Remote serviceObject) throws RMIHostException, RMIHostRuntimeException;

    /**
     * Unregister a service registered under the given service name and port number
     *
     * @param uri the URI of the server
     * @throws RMIHostException
     * @throws RMIHostRuntimeException
     */
    void unregisterService(String uri) throws RMIHostException, RMIHostRuntimeException;

    /**
     * find a remote service hosted on the given host, port and service name
     *
     * @param uri the URI of the service
     * @return the RMI server object
     * @throws RMIHostException
     * @throws RMIHostRuntimeException
     */
    Remote findService(String uri) throws RMIHostException, RMIHostRuntimeException;
}
