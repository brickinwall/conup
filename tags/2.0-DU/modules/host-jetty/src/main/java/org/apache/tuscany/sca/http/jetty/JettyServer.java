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
package org.apache.tuscany.sca.http.jetty;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.tuscany.sca.core.ExtensionPointRegistry;
import org.apache.tuscany.sca.core.LifeCycleListener;
import org.apache.tuscany.sca.core.UtilityExtensionPoint;
import org.apache.tuscany.sca.host.http.DefaultResourceServlet;
import org.apache.tuscany.sca.host.http.HttpScheme;
import org.apache.tuscany.sca.host.http.SecurityContext;
import org.apache.tuscany.sca.host.http.ServletHost;
import org.apache.tuscany.sca.host.http.ServletMappingException;
import org.apache.tuscany.sca.host.http.extensibility.HttpPortAllocator;
import org.apache.tuscany.sca.work.WorkScheduler;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.log.Log;
import org.mortbay.thread.ThreadPool;

/**
 * Implements an HTTP transport service using Jetty.
 *
 * @version $Rev: 1095242 $ $Date: 2011-04-20 01:19:13 +0100 (Wed, 20 Apr 2011) $
 */
public class JettyServer implements ServletHost, LifeCycleListener {
    private static final Logger logger = Logger.getLogger(JettyServer.class.getName());

    private final Object joinLock = new Object();
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;

    private String keyStoreType;
    private String trustStoreType;

    private boolean sendServerVersion;
    private WorkScheduler workScheduler;

    private HttpPortAllocator httpPortAllocator;

    // TODO - this static seems to be set by the JSORPC binding unit test
    //        doesn't look to be a great way of doing things
    public static int portDefault = 0;
    private int defaultPort;
    private int defaultSSLPort;

    /**
     * Represents a port and the server that serves it.
     */
    private class Port {
        private Server server;
        private ServletHandler servletHandler;

        private Port(Server server, ServletHandler servletHandler) {
            this.server = server;
            this.servletHandler = servletHandler;
        }

        public Server getServer() {
            return server;
        }

        public ServletHandler getServletHandler() {
            return servletHandler;
        }
    }

    private Map<Integer, Port> ports = new HashMap<Integer, Port>();

    private String contextPath = "/";
    private org.mortbay.log.Logger jettyLogger;

    public JettyServer(ExtensionPointRegistry registry) {
        UtilityExtensionPoint utilityExtensionPoint = registry.getExtensionPoint(UtilityExtensionPoint.class);
        this.workScheduler = utilityExtensionPoint.getUtility(WorkScheduler.class);
        this.httpPortAllocator = utilityExtensionPoint.getUtility(HttpPortAllocator.class);
        init();
    }

    protected JettyServer(WorkScheduler workScheduler, HttpPortAllocator httpPortAllocator) {
        this.httpPortAllocator = httpPortAllocator;
        this.workScheduler = workScheduler;
        init();
    }

    private void init() {
        this.defaultPort = this.httpPortAllocator.getDefaultPort(HttpScheme.HTTP);
        //handle backdoor to set specific default port in tests
        if(portDefault > 0) {
            this.defaultPort = portDefault;
        }
        this.defaultSSLPort = this.httpPortAllocator.getDefaultPort(HttpScheme.HTTPS);
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                trustStore = System.getProperty("javax.net.ssl.trustStore");
                trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                keyStore = System.getProperty("javax.net.ssl.keyStore");
                keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

                keyStoreType = System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
                trustStoreType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());

                System.setProperty("JETTY_NO_SHUTDOWN_HOOK", "true");
                return null;
            }
        });
    }

    public String getName() {
        return "jetty";
    }

    public void setDefaultPort(int port) {
        defaultPort = port;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setSendServerVersion(boolean sendServerVersion) {
        this.sendServerVersion = sendServerVersion;
    }

    /**
     * Stop all the started servers.
     */
    public void stop() {
        synchronized (joinLock) {
            joinLock.notifyAll();
        }
        try {
            Set<Entry<Integer, Port>> entries = new HashSet<Entry<Integer, Port>>(ports.entrySet());
            for (Entry<Integer, Port> entry : entries) {
                Port port = entry.getValue();
                Server server = port.getServer();
                server.stop();
                server.setStopAtShutdown(false);
                ports.remove(entry.getKey());
            }
        } catch (Exception e) {
            throw new ServletMappingException(e);
        } finally {
            if (jettyLogger != null) {
                Log.setLog(jettyLogger);
                jettyLogger = null;
            }
        }
    }

    private void configureSSL(SslSocketConnector connector, SecurityContext securityContext) {
        connector.setProtocol("TLS");
        if (securityContext != null) {
            keyStoreType = securityContext.getSSLProperties().getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
            keyStore = securityContext.getSSLProperties().getProperty("javax.net.ssl.keyStore");
            keyStorePassword = securityContext.getSSLProperties().getProperty("javax.net.ssl.keyStorePassword");

            trustStoreType = securityContext.getSSLProperties().getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
            trustStore = securityContext.getSSLProperties().getProperty("javax.net.ssl.trustStore");
            trustStorePassword = securityContext.getSSLProperties().getProperty("javax.net.ssl.trustStorePassword");
        }
        connector.setKeystore(keyStore);
        connector.setKeyPassword(keyStorePassword);
        connector.setKeystoreType(keyStoreType);

        connector.setTruststore(trustStore);
        connector.setTrustPassword(trustStorePassword);
        connector.setTruststoreType(trustStoreType);

        connector.setPassword(keyStorePassword);
        if (trustStore != null) {
            connector.setNeedClientAuth(true);
        }
    }

    public String addServletMapping(String suri, Servlet servlet) throws ServletMappingException {
        return addServletMapping(suri, servlet, null);
    }

    public String addServletMapping(String suri, Servlet servlet, final SecurityContext securityContext)
        throws ServletMappingException {
        URI uri = URI.create(suri);

        // Get the URI scheme and port
        String scheme = null;
        if (securityContext != null && securityContext.isSSLEnabled()) {
            scheme = "https";
        } else {
            scheme = uri.getScheme();
            if (scheme == null) {
                scheme = "http";
            }
        }

        String host = uri.getHost();
        if ("0.0.0.0".equals(host)) {
            host = null;
        }

        int portNumber = uri.getPort();
        if (portNumber == -1) {
            if ("http".equals(scheme)) {
                portNumber = defaultPort;
            } else {
                portNumber = defaultSSLPort;
            }
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {

            // Create and start a new server
            try {
                Server server = new Server();
                server.setThreadPool(new WorkSchedulerThreadPool());
                if ("https".equals(scheme)) {
                    //                    Connector httpConnector = new SelectChannelConnector();
                    //                    httpConnector.setPort(portNumber);
                    SslSocketConnector sslConnector = new SslSocketConnector();
                    sslConnector.setPort(portNumber);
                    // FIXME: [rfeng] We should set the host to be bound but binding-ws-axis2 is passing
                    // in an absolute URI with host set to one of the ip addresses
                    sslConnector.setHost(host);
                    configureSSL(sslConnector, securityContext);
                    server.setConnectors(new Connector[] {sslConnector});
                } else {
                    SelectChannelConnector selectConnector = new SelectChannelConnector();
                    selectConnector.setPort(portNumber);
                    // FIXME: [rfeng] We should set the host to be bound but binding-ws-axis2 is passing
                    // in an absolute URI with host set to one of the ip addresses
                    selectConnector.setHost(host);
                    server.setConnectors(new Connector[] {selectConnector});
                }

                ContextHandler contextHandler = new ContextHandler();
                //contextHandler.setContextPath(contextPath);
                contextHandler.setContextPath("/");
                server.setHandler(contextHandler);

                SessionHandler sessionHandler = new SessionHandler();
                ServletHandler servletHandler = new ServletHandler();
                sessionHandler.addHandler(servletHandler);

                contextHandler.setHandler(sessionHandler);

                server.setStopAtShutdown(true);
                server.setSendServerVersion(sendServerVersion);
                server.start();

                // Keep track of the new server and Servlet handler
                port = new Port(server, servletHandler);
                ports.put(portNumber, port);

            } catch (Exception e) {
                throw new ServletMappingException(e);
            }
        }

        // Register the Servlet mapping
        ServletHandler servletHandler = port.getServletHandler();
        ServletHolder holder;
        if (servlet instanceof DefaultResourceServlet) {

            // Optimize the handling of resource requests, use the Jetty default Servlet
            // instead of our default resource Servlet
            String servletPath = uri.getPath();
            if (servletPath.endsWith("*")) {
                servletPath = servletPath.substring(0, servletPath.length() - 1);
            }
            if (servletPath.endsWith("/")) {
                servletPath = servletPath.substring(0, servletPath.length() - 1);
            }
            if (!servletPath.startsWith("/")) {
                servletPath = '/' + servletPath;
            }

            DefaultResourceServlet resourceServlet = (DefaultResourceServlet)servlet;
            DefaultServlet defaultServlet = new JettyDefaultServlet(servletPath, resourceServlet.getDocumentRoot());
            holder = new ServletHolder(defaultServlet);

        } else {
            holder = new ServletHolder(servlet);
        }
        servletHandler.addServlet(holder);

        ServletMapping mapping = new ServletMapping();
        mapping.setServletName(holder.getName());
        String path = uri.getPath();

        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }

        mapping.setPathSpec(path);
        servletHandler.addServletMapping(mapping);

        // Compute the complete URL
        if (host == null) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                host = "localhost";
            }
        }
        URL addedURL;
        try {
            addedURL = new URL(scheme, host, portNumber, path);
        } catch (MalformedURLException e) {
            throw new ServletMappingException(e);
        }
        logger.info("Added Servlet mapping: " + addedURL);
        return addedURL.toString();
    }

    public URL getURLMapping(String suri, SecurityContext securityContext) throws ServletMappingException {
        return map(suri, securityContext, true);
    }

    private URL map(String suri, SecurityContext securityContext, boolean resolve) throws ServletMappingException {
        URI uri = URI.create(suri);

        // Get the URI scheme and port
        String scheme = null;
        if (securityContext != null && securityContext.isSSLEnabled()) {
            scheme = "https";
        } else {
            scheme = uri.getScheme();
            if (scheme == null) {
                scheme = "http";
            }
        }

        int portNumber = uri.getPort();
        if (portNumber == -1) {
            if ("http".equals(scheme)) {
                portNumber = defaultPort;
            } else {
                portNumber = defaultSSLPort;
            }
        }

        // Get the host
        String host = uri.getHost();
        if (host == null) {
            host = "0.0.0.0";
            if (resolve) {
                try {
                    host = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    host = "localhost";
                }
            }
        }

        // Construct the URL
        String path = uri.getPath();

        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }

        URL url;
        try {
            url = new URL(scheme, host, portNumber, path);
        } catch (MalformedURLException e) {
            throw new ServletMappingException(e);
        }
        return url;
    }

    public Servlet getServletMapping(String suri) throws ServletMappingException {

        if (suri == null) {
            return null;
        }

        URI uri = URI.create(suri);

        // Get the URI port
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {
            return null;
        }

        // Remove the Servlet mapping for the given Servlet
        ServletHandler servletHandler = port.getServletHandler();
        Servlet servlet = null;
        List<ServletMapping> mappings =
            new ArrayList<ServletMapping>(Arrays.asList(servletHandler.getServletMappings()));
        String path = uri.getPath();

        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }

        for (ServletMapping mapping : mappings) {
            if (Arrays.asList(mapping.getPathSpecs()).contains(path)) {
                try {
                    servlet = servletHandler.getServlet(mapping.getServletName()).getServlet();
                } catch (ServletException e) {
                    throw new IllegalStateException(e);
                }
                break;
            }
        }
        return servlet;
    }

    public Servlet removeServletMapping(String suri) {
        URI uri = URI.create(suri);

        // Get the URI port
        int portNumber = uri.getPort();
        if (portNumber == -1) {
            portNumber = defaultPort;
        }

        // Get the port object associated with the given port number
        Port port = ports.get(portNumber);
        if (port == null) {
            // TODO - EPR - SL commented out exception temporarily as the runtime is shared
            //              between multiple nodes in a VM and shutting down one node blows
            //              up any other nodes when they shut down.
            //throw new IllegalStateException("No servlet registered at this URI: " + suri);
            logger.warning("No servlet registered at this URI: " + suri);
            return null;
        }

        // Remove the Servlet mapping for the given Servlet
        ServletHandler servletHandler = port.getServletHandler();
        Servlet removedServlet = null;
        List<ServletMapping> mappings =
            new ArrayList<ServletMapping>(Arrays.asList(servletHandler.getServletMappings()));
        String path = uri.getPath();

        if (!path.startsWith("/")) {
            path = '/' + path;
        }

        if (!path.startsWith(contextPath)) {
            path = contextPath + path;
        }

        for (ServletMapping mapping : mappings) {
            if (Arrays.asList(mapping.getPathSpecs()).contains(path)) {
                try {
                    removedServlet = servletHandler.getServlet(mapping.getServletName()).getServlet();
                } catch (ServletException e) {
                    throw new IllegalStateException(e);
                }
                mappings.remove(mapping);
                logger.info("Removed Servlet mapping: " + path);
                break;
            }
        }
        if (removedServlet != null) {
            servletHandler.setServletMappings(mappings.toArray(new ServletMapping[mappings.size()]));

            // Stop the port if there are no servlet mappings on it anymore
            if (mappings.size() == 0) {
                try {
                    Server server = port.getServer();
                    server.stop();
                    server.setStopAtShutdown(false);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                ports.remove(portNumber);
            }

        } else {
            logger.warning("Trying to Remove servlet mapping: " + path + " where mapping is not registered");
        }

        return removedServlet;
    }

    public RequestDispatcher getRequestDispatcher(String suri) throws ServletMappingException {
        //FIXME implement this later
        return null;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String path) {
        this.contextPath = path;
    }

    /**
     * A wrapper to enable use of a WorkScheduler with Jetty
     */
    private class WorkSchedulerThreadPool implements ThreadPool {

        public boolean dispatch(Runnable work) {
            workScheduler.scheduleWork(work);
            return true;
        }

        public void join() throws InterruptedException {
            synchronized (joinLock) {
                joinLock.wait();
            }
        }

        public int getThreads() {
            throw new UnsupportedOperationException();
        }

        public int getIdleThreads() {
            throw new UnsupportedOperationException();
        }

        public boolean isLowOnThreads() {
            return false;
        }
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void start() {
        try {
            jettyLogger = new JettyLogger(logger);
            Log.setLog(jettyLogger);
        } catch (Throwable e) {
            // Ignore
        }
    }

    @Override
    public ServletContext getServletContext() {
        if (ports.size() > 0) {
            return ports.values().iterator().next().getServletHandler().getServletContext();
        } else {
            return null;
        }
    }

}
