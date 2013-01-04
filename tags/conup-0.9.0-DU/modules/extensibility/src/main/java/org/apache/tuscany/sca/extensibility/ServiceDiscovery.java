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

package org.apache.tuscany.sca.extensibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tuscany.sca.extensibility.impl.LDAPFilter;

/**
 * Service discovery for Tuscany based on J2SE Jar service provider spec.
 * Services are described using configuration files in META-INF/services.
 * Service description specifies a class name followed by optional properties.
 *
 * TODO: this is broken as it uses a static INSTANCE but non-static serviceAttributes
 * and discoverer so the same INSTANCE gets used across NodeFactories and picks up
 * old values
 *
 * @version $Rev: 1173363 $ $Date: 2011-09-20 21:55:35 +0100 (Tue, 20 Sep 2011) $
 * @tuscany.spi.extension.asclient
 */
public final class ServiceDiscovery implements ServiceDiscoverer {
    private final static Logger logger = Logger.getLogger(ServiceDiscovery.class.getName());
    private final static ServiceDiscovery INSTANCE = new ServiceDiscovery();

    private final Map<String, Map<String, String>> serviceAttributes = new HashMap<String, Map<String, String>>();
    private ServiceDiscoverer discoverer;

    private ServiceDiscovery() {
        super();
    }

    private ServiceDiscovery(ServiceDiscoverer discoverer) {
        super();
        this.discoverer = discoverer;
    }

    /**
     * Get an instance of Service discovery, one instance is created per
     * ClassLoader that this class is loaded from
     *
     * @return
     */
    public static ServiceDiscovery getInstance() {
        return INSTANCE;
    }
    
    public static ServiceDiscovery getInstance(ServiceDiscoverer discoverer) {
        return new ServiceDiscovery(discoverer);
    }

    public ServiceDiscoverer getServiceDiscoverer() {
        if (discoverer != null) {
            return discoverer;
        }
        try {
            // FIXME: This is a hack to trigger the activation of the extensibility-equinox bundle in OSGi
            Class.forName("org.apache.tuscany.sca.extensibility.equinox.EquinoxServiceDiscoverer");
            if (discoverer != null) {
                return discoverer;
            }
        } catch (Throwable e) {
        }
        discoverer = new ContextClassLoaderServiceDiscoverer(getClass().getClassLoader());
        return discoverer;
    }

    public void setServiceDiscoverer(ServiceDiscoverer sd) {
        if (discoverer != null && sd != null) {
            logger.warning("ServiceDiscoverer is reset to " + sd);
        }
        discoverer = sd;
    }

    public Collection<ServiceDeclaration> getServiceDeclarations(String name) throws IOException {
        return getServiceDeclarations(name, false);
    }

    public Collection<ServiceDeclaration> getServiceDeclarations(String name, boolean byRanking) throws IOException {
        Collection<ServiceDeclaration> declarations = getServiceDiscoverer().getServiceDeclarations(name);
        // declarations = removeDuplicateDeclarations(declarations);
        // Check if any of the service declarations has attributes that are overrided
        if (!serviceAttributes.isEmpty()) {
            for (ServiceDeclaration declaration : declarations) {
                Map<String, String> attrs = getAttributes(name);
                if (attrs != null) {
                    declaration.getAttributes().putAll(attrs);
                }
            }
        }
        if (!byRanking) {
            return declarations;
        }
        if (!declarations.isEmpty()) {
            List<ServiceDeclaration> declarationList = new ArrayList<ServiceDeclaration>(declarations);
            Collections.sort(declarationList, ServiceComparator.DESCENDING_ORDER);
            return declarationList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get the service declaration. If there are more than one services, the one with highest ranking will
     * be returned.
     */
    public ServiceDeclaration getServiceDeclaration(final String name) throws IOException {
        Collection<ServiceDeclaration> declarations = getServiceDeclarations(name, true);
        if (!declarations.isEmpty()) {
            // List<ServiceDeclaration> declarationList = new ArrayList<ServiceDeclaration>(declarations);
            // Collections.sort(declarationList, ServiceComparator.DESCENDING_ORDER);
            return declarations.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Get service declarations that are filtered by the service type. In an OSGi runtime, there
     * might be different versions of the services 
     * @param serviceType
     * @return
     * @throws IOException
     */
    public Collection<ServiceDeclaration> getServiceDeclarations(Class<?> serviceType, boolean byRanking)
        throws IOException {
        Collection<ServiceDeclaration> sds = getServiceDeclarations(serviceType.getName(), byRanking);
        for (Iterator<ServiceDeclaration> i = sds.iterator(); i.hasNext();) {
            ServiceDeclaration sd = i.next();
            if (!sd.isAssignableTo(serviceType)) {
                logger.log(Level.WARNING, "Service provider {0} is not a type of {1}", new Object[] {sd,serviceType.getName()});
                i.remove();
            }
        }
        return sds;
    }

    /**
     * Discover all service providers that are compatible with the service type
     * @param serviceType
     * @return
     * @throws IOException
     */
    public Collection<ServiceDeclaration> getServiceDeclarations(Class<?> serviceType) throws IOException {
        return getServiceDeclarations(serviceType, false);
    }
    
    /**
     * Discover all service providers that are compatible with the service type and match the filter
     * @param serviceType
     * @param filter
     * @return
     * @throws IOException
     */
    public Collection<ServiceDeclaration> getServiceDeclarations(Class<?> serviceType, String filter) throws IOException {
        Collection<ServiceDeclaration> sds = getServiceDeclarations(serviceType, false);
        Collection<ServiceDeclaration> filtered = new ArrayList<ServiceDeclaration>();
        LDAPFilter filterImpl = LDAPFilter.newInstance(filter);
        for(ServiceDeclaration sd: sds) {
            if(filterImpl.match(sd.getAttributes())) {
                filtered.add(sd);
            }
        }
        return filtered;
    }
    
    /**
     * @param serviceName
     * @param filter
     * @return
     * @throws IOException
     */
    public Collection<ServiceDeclaration> getServiceDeclarations(String serviceName, String filter) throws IOException {
        Collection<ServiceDeclaration> sds = getServiceDeclarations(serviceName, false);
        Collection<ServiceDeclaration> filtered = new ArrayList<ServiceDeclaration>();
        LDAPFilter filterImpl = LDAPFilter.newInstance(filter);
        for(ServiceDeclaration sd: sds) {
            if(filterImpl.match(sd.getAttributes())) {
                filtered.add(sd);
            }
        }
        return filtered;
    }

    public ServiceDeclaration getServiceDeclaration(Class<?> serviceType) throws IOException {
        Collection<ServiceDeclaration> sds = getServiceDeclarations(serviceType, true);
        if (sds.isEmpty()) {
            return null;
        } else {
            return sds.iterator().next();
        }
    }

    /**
     * Compare service declarations by ranking
     */
    private static class ServiceComparator implements Comparator<ServiceDeclaration> {
        private final static Comparator<ServiceDeclaration> DESCENDING_ORDER = new ServiceComparator();

        public int compare(ServiceDeclaration o1, ServiceDeclaration o2) {
            int rank1 = 0;
            String r1 = o1.getAttributes().get("ranking");
            if (r1 != null) {
                rank1 = Integer.parseInt(r1);
            }
            int rank2 = 0;
            String r2 = o2.getAttributes().get("ranking");
            if (r2 != null) {
                rank2 = Integer.parseInt(r2);
            }
            return rank2 - rank1; // descending
        }
    }

    public ClassLoader getContextClassLoader() {
        return discoverer.getContextClassLoader();
    }
    
    /**
     * Set the attributes for a given service type
     * @param serviceType
     * @param attributes
     */
    public void setAttribute(String serviceType, Map<String, String> attributes) {
        serviceAttributes.put(serviceType, attributes);
    }

    /**
     * Set an attribute to the given value for a service type
     * @param serviceType The service type
     * @param attribute The attribute name
     * @param value The attribute value
     */
    public void setAttribute(String serviceType, String attribute, String value) {
        Map<String, String> attributes = serviceAttributes.get(serviceType);
        if (attributes == null) {
            attributes = new HashMap<String, String>();
            serviceAttributes.put(serviceType, attributes);
        }
        attributes.put(attribute, value);
    }

    /**
     * Return a map of attributes for a given service type
     * @param serviceType
     * @return
     */
    public Map<String, String> getAttributes(String serviceType) {
        return serviceAttributes.get(serviceType);
    }
    
    /**
     * Remove the duplicate service declarations. The duplication happens when we have the same jar from more than one entries on the classpath
     * @param declarations
     * @return
     */
    public static Collection<ServiceDeclaration> removeDuplicateDeclarations(Collection<ServiceDeclaration> declarations) {
        // Use LinkedHashMap to maintain the insertion order
        Map<Map<String, String>, ServiceDeclaration> map = new LinkedHashMap<Map<String, String>, ServiceDeclaration>();
        for (ServiceDeclaration sd : declarations) {
            ServiceDeclaration existed = map.put(sd.getAttributes(), sd);
            if (existed != null) {
                logger.warning("Duplicate service declaration is detected: " + existed + " <-> " + sd);
            }
        }
        return map.values();
    }
}
