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

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * Default implementation of a RMI host.
 *
 * @version $Rev: 921690 $ $Date: 2010-03-11 04:45:13 +0000 (Thu, 11 Mar 2010) $
 */
public class DefaultRMIHost implements RMIHost {
    private final static Logger logger = Logger.getLogger(DefaultRMIHost.class.getName());
    private static final int CONNECTION_TIMEOUT = 3000; // 3 seconds

    // Map of RMI registries started and running
    private Map<String, Registry> rmiRegistries;
    
    private RMISocketFactory socketFactory;

    public DefaultRMIHost() {
        rmiRegistries = new ConcurrentHashMap<String, Registry>();
        this.socketFactory = new RMISocketFactoryImpl(CONNECTION_TIMEOUT);
    }
    
    public String registerService(String uri, Remote serviceObject) throws RMIHostException, RMIHostRuntimeException {
        RMIURI rmiURI = new RMIURI(uri);

        Registry registry;
        try {
            registry = rmiRegistries.get(Integer.toString(rmiURI.port));
            if (registry == null) {
                try {
                    registry = LocateRegistry.createRegistry(rmiURI.port, socketFactory, socketFactory);
                } catch (RemoteException e) {
                    registry = LocateRegistry.getRegistry(null, rmiURI.port, socketFactory);
                } 
                rmiRegistries.put(Integer.toString(rmiURI.port), registry);
            }
            registry.bind(rmiURI.serviceName, serviceObject);
            logger.info("RMI service registered: " + rmiURI);
            return rmiURI.toString();
        } catch (AlreadyBoundException e) {
            throw new RMIHostException(e);
        } catch (RemoteException e) {
            RMIHostRuntimeException rmiExec = new RMIHostRuntimeException(e.getMessage());
            rmiExec.setStackTrace(e.getStackTrace());
            throw rmiExec;
        }

    }

    public void unregisterService(String uri) throws RMIHostException, RMIHostRuntimeException {
        RMIURI rmiURI = new RMIURI(uri);

        try {
            Registry registry = rmiRegistries.get(Integer.toString(rmiURI.port));
            if (registry == null) {
                registry = LocateRegistry.getRegistry(null, rmiURI.port, socketFactory);
                rmiRegistries.put(Integer.toString(rmiURI.port), registry);
            }
            registry.unbind(rmiURI.serviceName);
            logger.info("RMI service unregistered: " + rmiURI);
        } catch (RemoteException e) {
            RMIHostRuntimeException rmiExec = new RMIHostRuntimeException(e.getMessage());
            rmiExec.setStackTrace(e.getStackTrace());
            throw rmiExec;
        } catch (NotBoundException e) {
            throw new RMIHostException(e.getMessage());
        }
    }

    public Remote findService(String uri) throws RMIHostException, RMIHostRuntimeException {
        RMIURI rmiURI = new RMIURI(uri);

        Remote remoteService = null;
        try {
            // Requires permission java.net.SocketPermission "host:port", "connect,accept,resolve"
            // in security policy.
            Registry registry = LocateRegistry.getRegistry(rmiURI.host, rmiURI.port, socketFactory);

            if (registry != null) {
                remoteService = registry.lookup(rmiURI.serviceName);
            }
        } catch (RemoteException e) {
            RMIHostRuntimeException rmiExec = new RMIHostRuntimeException(e.getMessage());
            rmiExec.setStackTrace(e.getStackTrace());
            throw rmiExec;
        } catch (NotBoundException e) {
            throw new RMIHostException(e.getMessage());
        }
        return remoteService;
    }

    /**
     * A representation of an RMI URI.
     *
     * rmi://[host][:port][/[object]]
     * rmi:[/][object]
     */
    private static class RMIURI {
        private String uriStr;
        private String host;
        private int port;
        private String serviceName;

        private RMIURI(String uriStr) {
            URI uri = URI.create(uriStr);
            host = uri.getHost();
            if (host == null) {
                try {
                    host = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    host = "localhost";
                }
            }
            port = uri.getPort();
            if (port <= 0) {
                port = RMI_DEFAULT_PORT;
            }
            String path = uri.getPath();
            if (path != null && path.charAt(0) == '/') {
                path = path.substring(1);
            }
            serviceName = path;
            this.uriStr = "rmi://" + host + ":" + port + "/" + serviceName;
        }

        public String toString() {
            return uriStr;
        }
    }

    public void stop() {
        for (Registry registry : rmiRegistries.values()) {
            try {
                UnicastRemoteObject.unexportObject(registry, false);
            } catch (NoSuchObjectException e) {
                e.printStackTrace();
            }
        }
        rmiRegistries.clear();
    }

}
