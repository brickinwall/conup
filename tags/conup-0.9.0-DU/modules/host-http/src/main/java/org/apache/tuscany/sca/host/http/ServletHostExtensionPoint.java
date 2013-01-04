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
package org.apache.tuscany.sca.host.http;

import java.util.List;

/**
 * An extension point for Servlet hosts.
 * 
 * @version $Rev: 922701 $ $Date: 2010-03-14 00:50:38 +0000 (Sun, 14 Mar 2010) $
 */
public interface ServletHostExtensionPoint {
    /**
     * Test if it's inside a web application
     * @return
     */
    boolean isWebApp();

    /**
     * Set the flag to indicate it's inside a web application
     * @param webApp
     */
    void setWebApp(boolean webApp);

    /**
     * Adds a Servlet host extension.
     * 
     * @param servletHost
     */
    void addServletHost(ServletHost servletHost);

    /**
     * Removes a Servlet host extension.
     * 
     * @param servletHost
     */
    void removeServletHost(ServletHost servletHost);
    
    /**
     * Returns a list of Servlet host extensions.
     * 
     * @return
     */
    List<ServletHost> getServletHosts();

}
