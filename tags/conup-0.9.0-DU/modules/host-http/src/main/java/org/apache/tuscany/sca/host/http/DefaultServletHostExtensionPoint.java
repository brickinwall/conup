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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.extensibility.ServiceDeclaration;
import org.apache.tuscany.sca.extensibility.ServiceHelper;

/**
 * Default implementation of a Servlet host extension point.
 * 
 * @version $Rev: 980218 $ $Date: 2010-07-28 22:01:41 +0100 (Wed, 28 Jul 2010) $
 */
public class DefaultServletHostExtensionPoint implements ServletHostExtensionPoint, LifeCycleListener {

    private List<ServletHost> servletHosts = new ArrayList<ServletHost>();
    private boolean loaded;
    private boolean webApp;

    private ExtensionPointRegistry registry;

    public DefaultServletHostExtensionPoint(ExtensionPointRegistry registry) {
        this.registry = registry;
    }

    public void addServletHost(ServletHost servletHost) {
        servletHosts.add(servletHost);
        if (servletHost instanceof LifeCycleListener) {
            ((LifeCycleListener)servletHost).start();
        }
    }

    public void removeServletHost(ServletHost servletHost) {
        servletHosts.remove(servletHost);
        if (servletHost instanceof LifeCycleListener) {
            ((LifeCycleListener)servletHost).stop();
        }
    }

    public List<ServletHost> getServletHosts() {
        loadServletHosts();
        return servletHosts;
    }

    private synchronized void loadServletHosts() {
        if (loaded)
            return;

        // Get the databinding service declarations
        Collection<ServiceDeclaration> sds;
        try {
            sds = registry.getServiceDiscovery().getServiceDeclarations(ServletHost.class, true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Load data bindings
        for (ServiceDeclaration sd : sds) {
            // Create a data binding wrapper and register it
            ServletHost servletHost = new LazyServletHost(sd);
            addServletHost(servletHost);
        }

        loaded = true;
    }

    /**
     * A data binding facade allowing data bindings to be lazily loaded and
     * initialized.
     */
    public class LazyServletHost implements ServletHost, LifeCycleListener {
        private ServiceDeclaration sd;
        private ServletHost host;

        /**
         * @param sd
         */
        public LazyServletHost(ServiceDeclaration sd) {
            super();
            this.sd = sd;
        }

        public synchronized ServletHost getServletHost() {
            if (host == null) {
                try {
                    host = ServiceHelper.newInstance(registry, sd);
                    ServiceHelper.start(host);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
            return host;
        }

        public String addServletMapping(String uri, Servlet servlet) throws ServletMappingException {
            return getServletHost().addServletMapping(uri, servlet);
        }
        
        public String addServletMapping(String uri, Servlet servlet, SecurityContext securityContext) throws ServletMappingException {
            return getServletHost().addServletMapping(uri, servlet, securityContext);
        }        

        public String getContextPath() {
            return getServletHost().getContextPath();
        }

        public int getDefaultPort() {
            return getServletHost().getDefaultPort();
        }

        public RequestDispatcher getRequestDispatcher(String uri) throws ServletMappingException {
            return getServletHost().getRequestDispatcher(uri);
        }

        public Servlet getServletMapping(String uri) throws ServletMappingException {
            return getServletHost().getServletMapping(uri);
        }

        public URL getURLMapping(String uri, SecurityContext securityContext) {
            return getServletHost().getURLMapping(uri, securityContext);
        }

        public Servlet removeServletMapping(String uri) throws ServletMappingException {
            return getServletHost().removeServletMapping(uri);
        }

        public void setAttribute(String name, Object value) {
            getServletHost().setAttribute(name, value);
        }

        public void setContextPath(String path) {
            getServletHost().setContextPath(path);
        }

        public void setDefaultPort(int port) {
            getServletHost().setDefaultPort(port);
        }
        
        public String getName() {
            return sd.getAttributes().get("name");
        }

        public void start() {
        }

        public void stop() {
            ServiceHelper.stop(host);
        }

        @Override
        public ServletContext getServletContext() {
            return getServletHost().getServletContext();
        }
    }

    public void start() {
    }

    public void stop() {
        ServiceHelper.stop(servletHosts);
        servletHosts.clear();
        registry = null;
    }

    public boolean isWebApp() {
        return webApp;
    }

    public void setWebApp(boolean webApp) {
        this.webApp = webApp;
    }
}
