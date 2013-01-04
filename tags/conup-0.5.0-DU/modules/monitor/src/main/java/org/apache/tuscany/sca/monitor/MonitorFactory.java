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
package org.apache.tuscany.sca.monitor;


/**
 * A factory for validation monitors
 * 
 * @version $Rev: 828119 $ $Date: 2009-10-21 19:00:07 +0100 (Wed, 21 Oct 2009) $
 */
public interface MonitorFactory {

    /**
     * Create a new monitor.
     * 
     * @return a new monitor
     */
    Monitor createMonitor();

    /**
     * Get the monitor instance on the thread
     * @return the monitor instance on the thread
     */
    Monitor getContextMonitor();

    /**
     * Get the monitor instance on the thread
     * @param create if it true, then create a new instance if no monitor is on the thread 
     * @return the monitor instance on the thread
     */
    Monitor getContextMonitor(boolean create);

    /**
     * Remove the monitor on the thread
     * @return The existing instance
     */
    Monitor removeContextMonitor();

    /**
     * Set the monitor onto the thread
     * @param value The new instance
     * @return The old instance
     */
    Monitor setContextMonitor(Monitor monitor);
}
