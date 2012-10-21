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

import org.apache.tuscany.sca.monitor.impl.MonitorImpl;

/**
 * A factory for creating validation monitors
 *
 * @version $Rev: 828119 $ $Date: 2009-10-21 19:00:07 +0100 (Wed, 21 Oct 2009) $
 */
public class DefaultMonitorFactory implements MonitorFactory {
    private ThreadLocal<Monitor> contextMonitor = new InheritableThreadLocal<Monitor>();

    public Monitor createMonitor() {
        return new MonitorImpl();
    }

    public Monitor getContextMonitor() {
        return contextMonitor.get();
    }

    public Monitor getContextMonitor(boolean create) {
        Monitor monitor = contextMonitor.get();
        if (monitor == null) {
            monitor = new MonitorImpl();
            setContextMonitor(monitor);
        }
        return monitor;
    }

    public Monitor removeContextMonitor() {
        Monitor old = contextMonitor.get();
        contextMonitor.remove();
        return old;
    }

    public Monitor setContextMonitor(Monitor value) {
        Monitor old = contextMonitor.get();
        contextMonitor.set(value);
        return old;
    }
}
