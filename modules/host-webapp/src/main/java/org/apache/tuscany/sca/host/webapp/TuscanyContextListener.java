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

package org.apache.tuscany.sca.host.webapp;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * A ServletContextListener to create and close the SCADomain
 * when the webapp is initialized or destroyed.
 */
public class TuscanyContextListener implements ServletContextListener {
    private final Logger logger = Logger.getLogger(TuscanyContextListener.class.getName());
    private boolean inited;

    public void contextInitialized(ServletContextEvent event) {
        logger.info(event.getServletContext().getServletContextName() + " is starting.");
        try {
            WebContextConfigurator configurator = WebAppHelper.getConfigurator(event.getServletContext());
            WebAppHelper.init(configurator);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        inited = true;
    }

    public void contextDestroyed(ServletContextEvent event) {
        logger.info(event.getServletContext().getServletContextName() + " is stopping.");
        if (!inited) {
            return;
        }
        try {
            WebContextConfigurator configurator = WebAppHelper.getConfigurator(event.getServletContext());
            WebAppHelper.stop(configurator);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        inited = false;
    }

}
