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

package org.apache.tuscany.sca.node.launcher;

import static org.apache.tuscany.sca.node.launcher.NodeLauncherUtil.domainManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple launcher for the SCA domain manager. 
 *
 * @version $Rev: 720905 $ $Date: 2008-11-26 15:47:28 +0000 (Wed, 26 Nov 2008) $
 */
public class DomainManagerLauncher {
    
    static final Logger logger = Logger.getLogger(DomainManagerLauncher.class.getName());    

    /**
     * Constructs a new DomainManagerLauncher.
     */
    private DomainManagerLauncher() {
    }
    
    /**
     * Returns a new launcher instance.
     *  
     * @return a new launcher instance
     */
    public static DomainManagerLauncher newInstance() {
        return new DomainManagerLauncher();
    }

    /**
     * Creates a new DomainManager.
     * 
     * @return a new DomainManager
     * @throws LauncherException
     */
    public <T> T createDomainManager() throws LauncherException {
        return (T)domainManager(".");
    }
    
    /**
     * Creates a new DomainManager.
     * 
     * @param rootDirectory the domain's root configuration directory 
     * 
     * @return a new DomainManager
     * @throws LauncherException
     */
    public <T> T createDomainManager(String rootDirectory) throws LauncherException {
        return (T)domainManager(rootDirectory);
    }
    
    public static void main(String[] args) throws Exception {
        logger.info("Apache Tuscany SCA Domain Manager is starting...");

        // Create a launcher
        DomainManagerLauncher launcher = newInstance();
        
        Object domainManager = null;
        ShutdownThread shutdown = null;
        try {
            while (true) {
                
                // Start the domain manager
                domainManager = launcher.createDomainManager();
                try {
                    domainManager.getClass().getMethod("start").invoke(domainManager);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "SCA Domain Manager could not be started", e);
                    throw e;
                }
                logger.info("SCA Domain Manager is now started.");
                
                // Install a shutdown hook
                shutdown = new ShutdownThread(domainManager);
                Runtime.getRuntime().addShutdownHook(shutdown);
                
                logger.info("Press 'q' to quit, 'r' to restart.");
                int k = 0;
                try {
                    while ((k != 'q') && (k != 'r')) {
                        k = System.in.read();
                    }
                } catch (IOException e) {
                    
                    // Wait forever
                    Object lock = new Object();
                    synchronized(lock) {
                        lock.wait();
                    }
                }

                // Stop the domain manager
                if (domainManager != null) {
                    Object dm = domainManager;
                    domainManager = null;
                    stopDomainManager(dm);
                }
                
                // Quit
                if (k == 'q' ) {
                    break;
                }
            }
        } catch (Exception e) {
            // Stop the domain manager
            if (domainManager != null) {
                try {
                    Object dm = domainManager;
                    domainManager = null;
                    stopDomainManager(dm);
                } catch (Exception e2) {
                }
            }
            throw e;
            
        } finally {
            
            // Remove the shutdown hook
            if (shutdown != null) {
                Runtime.getRuntime().removeShutdownHook(shutdown);
            }
        }
    }

    /**
     * Stop the given domain manager.
     * 
     * @param domainManager
     * @throws Exception
     */
    private static void stopDomainManager(Object domainManager) throws Exception {
        try {
            domainManager.getClass().getMethod("stop").invoke(domainManager);
            logger.info("SCA Domain Manager is now stopped.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SCA Domain Manager could not be stopped", e);
            throw e;
        }
    }
    
    private static class ShutdownThread extends Thread {
        private Object domainManager;

        public ShutdownThread(Object domainManager) {
            super();
            this.domainManager = domainManager;
        }

        @Override
        public void run() {
            try {
                stopDomainManager(domainManager);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
