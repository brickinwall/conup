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

import java.util.List;

/**
 * @version $Rev: 685909 $ $Date: 2008-08-14 15:17:15 +0100 (Thu, 14 Aug 2008) $
 * An extension point for CORBA hostst
 */
public interface CorbaHostExtensionPoint {

    /**
     * Removes CORBA host extension
     * 
     * @param corbaHost
     */
    void addCorbaHost(CorbaHost corbaHost);

    /**
     * Removes CORBA host extension
     * 
     * @param corbaHost
     */
    void removeCorbaHost(CorbaHost corbaHost);

    /**
     * Gets all CORBA host extensions
     * 
     * @return
     */
    List<CorbaHost> getCorbaHosts();

}
