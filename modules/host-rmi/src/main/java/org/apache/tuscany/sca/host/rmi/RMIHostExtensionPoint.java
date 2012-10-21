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

import java.util.List;

/**
 * An extension point for RMI hosts.
 * 
 * @version $Rev: 639609 $ $Date: 2008-03-21 12:53:25 +0000 (Fri, 21 Mar 2008) $
 */
public interface RMIHostExtensionPoint {

    /**
     * Adds a Servlet host extension.
     * 
     * @param rmiHost
     */
    void addRMIHost(RMIHost rmiHost);

    /**
     * Removes a Servlet host extension.
     * 
     * @param rmiHost
     */
    void removeRMIHost(RMIHost rmiHost);
    
    /**
     * Returns a list of Servlet host extensions.
     * 
     * @return
     */
    List<RMIHost> getRMIHosts();

}
